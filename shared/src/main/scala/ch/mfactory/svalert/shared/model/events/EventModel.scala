package ch.mfactory.svalert.shared.model.events

import cats.Show
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object EventModel {

  sealed trait KafkaMessageKey
  sealed trait KafkaMessageValue

  sealed trait Subscription {
    def fold[A](onSimpleSubscription: SimpleSubscription => A): A = this match {
      case s @ SimpleSubscription(_) => onSimpleSubscription(s)
    }
  }

  final case object EmptyKey extends KafkaMessageKey
  final case class IdKey(id: String, kind: String, service: String) extends KafkaMessageKey

  final case class SimpleSubscription(value: String) extends Subscription

  sealed trait SubscriptionEvent extends KafkaMessageValue {
    def fold[X](onSubscribe: SubscribeEvent => X, onUnsubscribe: UnsubscribeEvent => X): X = this match {
      case event @ SubscribeEvent(_, _) => onSubscribe(event)
      case event @ UnsubscribeEvent(_, _) => onUnsubscribe(event)
    }
  }

  final case class ChatId(unwrap: Long)

  implicit val show: Show[ChatId] = Show.fromToString

  final case class SubscribeEvent(chatId: ChatId, subscription: Subscription) extends SubscriptionEvent
  final case class UnsubscribeEvent(chatId: ChatId, subscription: Subscription) extends SubscriptionEvent

  implicit lazy val eventKeyDecoder: Decoder[KafkaMessageKey] = deriveDecoder
  implicit lazy val eventKeyEncoder: Encoder[KafkaMessageKey] = deriveEncoder

  implicit lazy val eventValueDecoder: Decoder[KafkaMessageValue] = deriveDecoder
  implicit lazy val eventValueEncoder: Encoder[KafkaMessageValue] = deriveEncoder

  implicit lazy val subscriptionDecoder: Decoder[Subscription] = deriveDecoder
  implicit lazy val subscriptionEncoder: Encoder[Subscription] = deriveEncoder

  implicit lazy val idKeyDecoder: Decoder[IdKey] = deriveDecoder
  implicit lazy val idKeyEncoder: Encoder[IdKey] = deriveEncoder

  implicit lazy val emptyKeyDecoder: Decoder[EmptyKey.type] = deriveDecoder
  implicit lazy val emptyKeyEncoder: Encoder[EmptyKey.type] = deriveEncoder



  implicit lazy val simpleSubscriptionDecoder: Decoder[SimpleSubscription] = deriveDecoder
  implicit lazy val simpleSubscriptionEncoder: Encoder[SimpleSubscription] = deriveEncoder

  implicit lazy val chatIdDecoder: Decoder[ChatId] = deriveDecoder
  implicit lazy val chatIdEncoder: Encoder[ChatId] = deriveEncoder

  implicit lazy val subscriptionEventDecoder: Decoder[SubscriptionEvent] = deriveDecoder
  implicit lazy val subscriptionEventEncoder: Encoder[SubscriptionEvent] = deriveEncoder

  implicit lazy val subscribeDecoder: Decoder[SubscribeEvent] = deriveDecoder
  implicit lazy val subscribeEncoder: Encoder[SubscribeEvent] = deriveEncoder

  implicit lazy val unsubscribeDecoder: Decoder[UnsubscribeEvent] = deriveDecoder
  implicit lazy val unsubscribeEncoder: Encoder[UnsubscribeEvent] = deriveEncoder


}
