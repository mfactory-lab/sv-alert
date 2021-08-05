package ch.mfactory.svalert.telegramBot.notifications

import ch.mfactory.svalert.shared.kafka.consumer.JsonConsumerDsl
import ch.mfactory.svalert.shared.model.events.EventModel.EmptyKey
import ch.mfactory.svalert.shared.model.events.Notifications.KapacitorNotification

trait NotificationsDsl[F[_]] {

  def consumeKN(callback: KapacitorNotification => F[Unit]): F[Unit]
}

object NotificationsDsl {
  def apply[F[_]](implicit ev: NotificationsDsl[F]): NotificationsDsl[F] = ev

  def interpreter[
    F[_] : JsonConsumerDsl
  ](): NotificationsDsl[F] = new NotificationsDsl[F] {
    override def consumeKN(callback: KapacitorNotification => F[Unit]): F[Unit] =
      JsonConsumerDsl[F].consumeF[EmptyKey.type, KapacitorNotification]{ case (_, kn) =>
        callback(kn)
      }
  }
}
