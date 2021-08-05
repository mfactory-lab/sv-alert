package ch.mfactory.svalert.telegramBot.model

import cats.Show
import ch.mfactory.svalert.shared.model.events.EventModel.{ChatId, SimpleSubscription, Subscription}

final case class Subscriptions(value: Map[SimpleSubscription, Set[ChatId]]) {

  def add(chatId: Long, subscription: Subscription): Subscriptions = {
    subscription.fold(
      onSimpleSubscription = s => {
        Subscriptions(value + (s -> (value.getOrElse(s, Set()) + ChatId(chatId))))
      }
    )
  }


  def remove(chatId: Long, subscription: Subscription): Subscriptions =
    subscription.fold(
      onSimpleSubscription = s => {
        Subscriptions(
          value.get(s)
            .map(_ - ChatId(chatId))
            .map(chats =>
              if (chats.isEmpty)
                value - s
              else
                value + (s -> chats)
            )
            .getOrElse(value)
        )
      }
    )

}

object Subscriptions {
  def empty: Subscriptions = Subscriptions(Map())

  implicit val show: Show[Subscriptions] = Show.fromToString

}
