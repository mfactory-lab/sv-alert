package ch.mfactory.svalert.telegramBot.bot

import cats.effect.Sync
import com.bot4s.telegram.api.declarative.{Args, CommandFilterMagnet, Commands, RegexCommands}
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.{ChatId, Message}
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import cats.implicits._

trait BotDsl[F[_]] {

  def sendMessage(chatId: Long, message: String): F[Unit]
  def run(): F[Unit]
  def registerOnCommandHandler(pattern: CommandFilterMagnet)(handler: (Commands[F], Message) => F[Unit]): F[Unit]

  final def registerOnCommandMessageHandler(pattern: CommandFilterMagnet)(handler: Message => F[Unit]): F[Unit] =
    registerOnCommandHandler(pattern){case (_, message) => handler(message)}
  final def registerOnCommandArgsHandler(pattern: CommandFilterMagnet)(handler: (Message, Args) => F[Unit]): F[Unit]  =
    registerOnCommandHandler(pattern){ case (command, message) =>
      implicit val badImplicit: Message = message
      command.withArgs(handler(message, _))
    }

}

object BotDsl {

  def apply[F[_]](implicit ev: BotDsl[F]): BotDsl[F] = ev

  def interpreter[
    F[_] : Sync
  ](backend: SttpBackend[F, Fs2Streams[F]],
    token: String): BotDsl[F] = new BotDsl[F] {

    val bot = new TelegramBot[F](token, backend)
        with Polling[F]
        with Commands[F]
        with RegexCommands[F]

//    class Tst extends TelegramBot[F](token, backend)
//      with Polling[F]
//      with Commands[F]
//      with RegexCommands[F]
//    {
//      onCommand("hola") { implicit msg =>
//        withArgs { args: Args =>
//          reply("Hola Mundo!").void
//        }
//        using(_.from){ x: User =>
//
//        }
//      }
//
//    }

    override def registerOnCommandHandler(pattern: CommandFilterMagnet)(handler: (Commands[F], Message) => F[Unit]): F[Unit] =
      Sync[F].delay(
        bot.onCommand(pattern){ message =>
          handler(bot, message)
        }
      )

    override def sendMessage(chatId: Long, message: String): F[Unit] =
      bot.request(SendMessage(ChatId(chatId), message)).void

    override def run(): F[Unit] = bot.run()
  }


}
