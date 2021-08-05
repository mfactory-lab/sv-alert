package ch.mfactory.svalert.telegramBot.subscription

import ch.mfactory.svalert.shared.kafka.consumer.JsonConsumerDsl
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import ch.mfactory.svalert.shared.model.events.EventModel._
import mfactory.ch.buildinfo.BuildInfo

trait SubscriptionDsl[F[_]] {

  def subscribe(chatId: Long, subscription: Subscription): F[Unit]
  def unsubscribe(chatId: Long, subscription: Subscription): F[Unit]
  def consume(callBack: ((Long, SubscriptionEvent)) => F[Unit]): F[Unit]

}

object SubscriptionDsl {

  def apply[F[_]](implicit ev: SubscriptionDsl[F]): SubscriptionDsl[F] = ev

  def interpreter[
    F[_] : JsonProducerDsl : JsonConsumerDsl
  ](): SubscriptionDsl[F] = new SubscriptionDsl[F] {
    override def subscribe(chatId: Long, subscription: Subscription): F[Unit] = {
      JsonProducerDsl[F].produce[IdKey, SubscriptionEvent](
        IdKey(chatId.toString, subscription.getClass.getSimpleName, BuildInfo.name),
        SubscribeEvent(ChatId(chatId), subscription)
      )
    }

    override def unsubscribe(chatId: Long, subscription: Subscription): F[Unit] = {
      JsonProducerDsl[F].produce[IdKey, SubscriptionEvent](
        IdKey(chatId.toString, subscription.getClass.getSimpleName, BuildInfo.name),
        UnsubscribeEvent(ChatId(chatId), subscription)
      )
    }

    override def consume(callBack: ((Long, SubscriptionEvent)) => F[Unit]): F[Unit] =
      JsonConsumerDsl[F].consumeF[IdKey, SubscriptionEvent]{ case (key, value) =>
        callBack((key.id.toLongOption.getOrElse(-1), value))
      }

  }
}
