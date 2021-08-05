package ch.mfactory.svalert.telegramBot.bot

import cats.effect.concurrent.Ref
import cats.MonadError
import ch.mfactory.svalert.shared.model.events.EventModel.SimpleSubscription
import ch.mfactory.svalert.telegramBot.subscription.SubscriptionDsl
import cats.implicits._
import ch.mfactory.svalert.shared.model.events.EventModel
import ch.mfactory.svalert.telegramBot.model.Subscriptions
import com.bot4s.telegram.api.declarative.CommandImplicits
import mfactory.ch.buildinfo.BuildInfo

object BotService {

  private val commandImplicits = new CommandImplicits{}
  import commandImplicits._

  def registerOnCommandPingPongHandler[
    F[_] : BotDsl
  ](): F[Unit] =
  {
    BotDsl[F].registerOnCommandMessageHandler("/ping"){ message =>

      BotDsl[F].sendMessage(message.chat.id, "pong")

    }

  }

  def registerOnCommandInfoHandler[
    F[_] : BotDsl
  ](): F[Unit] =
  {
    val infoMessage =
      s"""
        |bot: ${BuildInfo.name}
        |version: ${BuildInfo.version}
        |build by Joogh Validator https://joogh.io from mFactory Team
        |to use with sv-manager https://sv-manager.thevalidators.io/
        |""".stripMargin

    BotDsl[F].registerOnCommandMessageHandler("/info" | "/start"){ message =>

      BotDsl[F].sendMessage(message.chat.id, infoMessage)

    }

  }

  def registerOnCommandHelpHandler[
    F[_] : BotDsl
  ](): F[Unit] =
  {
    val helpMessage =
      """
        |/help for Help Message
        |/subscribe {identity key}
        |/unsubscribe {identity key}
        |/list
        |""".stripMargin

    BotDsl[F].registerOnCommandMessageHandler("/help" | "/start"){ message =>

      BotDsl[F].sendMessage(message.chat.id, helpMessage)

    }

  }

  def registerOnCommandSubscribeHandler[
    F[_] : BotDsl : SubscriptionDsl : MonadError[*[_], Throwable]
  ](): F[Unit] =
  {
    BotDsl[F].registerOnCommandArgsHandler("/subscribe"){ case (message, args) =>

      BotDsl[F].sendMessage(message.chat.id, s"args: ${args.mkString(", ")}") >>
      args.headOption.traverse_{ id =>
        SubscriptionDsl[F].subscribe(message.chat.id, SimpleSubscription(id))
      }
    }
  }

  def registerOnCommandUnSubscribeHandler[
    F[_] : BotDsl : SubscriptionDsl : MonadError[*[_], Throwable]
  ](): F[Unit] =
  {
    BotDsl[F].registerOnCommandArgsHandler("/unsubscribe"){ case (message, args) =>

      BotDsl[F].sendMessage(message.chat.id, s"args: ${args.mkString(", ")}") >>
      args.headOption.traverse_{ id =>
        SubscriptionDsl[F].unsubscribe(message.chat.id, SimpleSubscription(id))
      }
    }
  }

  def registerOnCommandListHandler[
    F[_] : BotDsl : SubscriptionDsl : MonadError[*[_], Throwable]
  ](subscriptionsR: Ref[F, Subscriptions]): F[Unit] =
  {
    BotDsl[F].registerOnCommandMessageHandler("/list"){ message =>
      subscriptionsR.get.flatMap{ subscriptions =>
        val result = subscriptions.value
          .filter(_._2.contains(EventModel.ChatId(message.chat.id)))
          .keys
          .mkString("(", ", ", ")")

        BotDsl[F].sendMessage(message.chat.id, result)
      }
    }
  }

}
