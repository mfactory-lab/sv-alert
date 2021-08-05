package ch.mfactory.svalert.telegramBot.notifications

import cats.MonadError
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import ch.mfactory.svalert.shared.kafka.consumer.JsonConsumerDsl
import ch.mfactory.svalert.telegramBot.bot.BotDsl
import ch.mfactory.svalert.telegramBot.config.Config
import fs2.kafka.KafkaConsumer
import io.circe.Json
import cats.implicits._
import ch.mfactory.svalert.shared.model.events.EventModel.SimpleSubscription
import ch.mfactory.svalert.telegramBot.model.Subscriptions

object NotificationService {

  def buildNotificationDsl[
    F[_] : Concurrent
  ](config: Config,
    consumer: KafkaConsumer[F, Json, Json],
    timer: Timer[F]): NotificationsDsl[F] =
  {
    implicit val consumerDsl: JsonConsumerDsl[F] =
      JsonConsumerDsl.interpreter(config.notificationsConsumer, consumer, timer)

    NotificationsDsl.interpreter()
  }

  def consumeNotificationEvent[
    F[_] : NotificationsDsl : BotDsl : MonadError[*[_], Throwable]
  ](subscriptionsR: Ref[F, Subscriptions]): F[Unit] =
  {
    NotificationsDsl[F].consumeKN{ notification =>
      subscriptionsR.get.flatMap{ subscriptions =>
        subscriptions.value.getOrElse(SimpleSubscription(notification.id), Set()).toList
        .traverse(chatId =>
          BotDsl[F].sendMessage(chatId.unwrap, s"${notification.id}: ${notification.value}")
        )
      }.void
    }
  }

}
