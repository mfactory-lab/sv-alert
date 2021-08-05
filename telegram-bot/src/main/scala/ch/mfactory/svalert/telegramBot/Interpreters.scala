package ch.mfactory.svalert.telegramBot

import cats.effect.{Concurrent, Timer}
import ch.mfactory.svalert.shared.kafka.consumer.JsonConsumerDsl
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import ch.mfactory.svalert.telegramBot.bot.BotDsl
import ch.mfactory.svalert.telegramBot.config.Config
import ch.mfactory.svalert.telegramBot.notifications.{NotificationService, NotificationsDsl}
import ch.mfactory.svalert.telegramBot.subscription.{SubscriptionDsl, SubscriptionService}
import fs2.kafka.{KafkaConsumer, KafkaProducer}
import io.circe.Json
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend

trait Interpreters[F[_]] {

  implicit def subscriptionDslInterpreter: SubscriptionDsl[F]
  implicit def notificationsDslInterpreter: NotificationsDsl[F]
  implicit def botDslInterpreter: BotDsl[F]

}

object Interpreters {

  def apply[
    F[_] : Concurrent
  ](config: Config,
    subscriptionProducer: KafkaProducer[F, Json, Json],
    subscriptionConsumer: KafkaConsumer[F, Json, Json],
    notificationConsumer: KafkaConsumer[F, Json, Json],
    partitionCount: Int,
    timer: Timer[F],
    sttpBackend: SttpBackend[F, Fs2Streams[F]]): Interpreters[F] = new Interpreters[F]
  {

    override implicit def botDslInterpreter: BotDsl[F] =
      BotDsl.interpreter(sttpBackend, config.bot.token)

    override implicit def subscriptionDslInterpreter: SubscriptionDsl[F] =
      SubscriptionService.buildSubscriptionDsl(
        config,
        subscriptionProducer,
        partitionCount,
        subscriptionConsumer,
        timer)

    override implicit def notificationsDslInterpreter: NotificationsDsl[F] =
      NotificationService.buildNotificationDsl(config, notificationConsumer, timer)
  }

}
