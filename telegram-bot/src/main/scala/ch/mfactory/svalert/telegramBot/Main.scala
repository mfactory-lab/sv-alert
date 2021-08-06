package ch.mfactory.svalert.telegramBot

import cats.MonadError
import cats.effect.concurrent.Ref
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync, Timer}
import ch.mfactory.svalert.shared.config.ConfigService
import ch.mfactory.svalert.telegramBot.bot.{BotDsl, BotService}
import ch.mfactory.svalert.telegramBot.config.Config
import fs2.Stream
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.http4s.Http4sBackend
import cats.implicits._
import ch.mfactory.svalert.shared.kafka.consumer.JsonConsumerDsl
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import ch.mfactory.svalert.telegramBot.model.Subscriptions
import ch.mfactory.svalert.telegramBot.notifications.{NotificationService, NotificationsDsl}
import ch.mfactory.svalert.telegramBot.subscription.{SubscriptionDsl, SubscriptionService}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
      Blocker[IO].use { blocker =>

        Http4sBackend
          .usingDefaultBlazeClientBuilder[IO](blocker)
          .use{ backend: SttpBackend[IO, Fs2Streams[IO]] =>
            val ec = blocker.blockingContext
            val cs = IO.contextShift(ec)
            val timer = IO.timer(ec)

            runF[IO](backend, timer, cs, blocker)




          }

      }

  }

  private def runF[
    F[_] : ConcurrentEffect
  ](sttpBackend: SttpBackend[F, Fs2Streams[F]],
    timer: Timer[F],
    cs: ContextShift[F],
    blocker: Blocker): F[ExitCode] = {
    ConfigService.produceConfig[F, Config](ConfigService.defaultConfigName, blocker, cs).flatMap { config =>

      Ref.of[F, Subscriptions](Subscriptions.empty).flatMap { ref =>

        val resources = for {
          subscriptionProducer <- JsonProducerDsl.createProducer(config.subscriptionProducer, cs)
          subscriptionPartitionCount <- Resource.eval(JsonProducerDsl.partitionCount(config.subscriptionProducer, cs))
          subscriptionConsumer <- JsonConsumerDsl.createConsumer(config.subscriptionConsumer, cs, timer)
          notificationConsumer <- JsonConsumerDsl.createConsumer(config.notificationsConsumer, cs, timer)
        } yield (
          subscriptionProducer,
          subscriptionPartitionCount,
          subscriptionConsumer,
          notificationConsumer)

        resources.use { case (subscriptionProducer, subscriptionPartitionCount, subscriptionConsumer, notificationConsumer) =>

          val interpreters = Interpreters[F](config,
            subscriptionProducer,
            subscriptionConsumer,
            notificationConsumer,
            subscriptionPartitionCount,
            timer,
            sttpBackend)

          import interpreters._


          runApplicationF(timer, ref)

        }
      }
    }
  }

  private def runApplicationF[
    F[_] : ConcurrentEffect : SubscriptionDsl : NotificationsDsl : BotDsl
  ](timer: Timer[F],
    ref: Ref[F, Subscriptions]): F[ExitCode] =
  {

    Stream.empty.repeat
//      .concurrently(SchedulerService.schedulerStream(timer, ref))
      .concurrently(startBackgroundService(
          BotService.registerOnCommandPingPongHandler() >>
          BotService.registerOnCommandInfoHandler() >>
          BotService.registerOnCommandHelpHandler() >>
          BotService.registerOnCommandSubscribeHandler() >>
          BotService.registerOnCommandUnSubscribeHandler() >>
          BotService.registerOnCommandListHandler(ref) >>
          BotDsl[F].run()
      ))
      .concurrently(startBackgroundService(
          SubscriptionService.consumeSubscriptionEvents(ref)
      ))
      .concurrently(startBackgroundService(
        NotificationService.consumeNotificationEvent(ref)
      ))
      .compile
      .last
      .map(_ => ExitCode.Success)
  }


  private def startBackgroundService[
    F[_]: Sync
  ](service: F[Unit]): Stream[F, Unit] = {
    val stream = Stream.eval(service)

    stream
      .handleErrorWith{ t =>
        Stream
          .emit(println(t.getLocalizedMessage))
          .append(stream)
      }
  }

}
