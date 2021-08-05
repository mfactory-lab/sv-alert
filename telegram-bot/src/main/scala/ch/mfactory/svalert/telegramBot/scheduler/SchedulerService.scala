package ch.mfactory.svalert.telegramBot.scheduler

import cats.effect.concurrent.Ref
import cats.effect.{Sync, Timer}
import cron4s.Cron
import cron4s.expr.CronExpr
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import eu.timepit.fs2cron.{ScheduledStreams, Scheduler}
import cats.implicits._
import ch.mfactory.svalert.shared.model.events.EventModel
import ch.mfactory.svalert.telegramBot.bot.BotDsl
import ch.mfactory.svalert.telegramBot.model.Subscriptions

object SchedulerService {

  def schedulerStream[
    F[_] : Sync : BotDsl
  ](timer: Timer[F],
    subscriptionsR: Ref[F, Subscriptions]): fs2.Stream[F, Unit] = {

    implicit lazy val badImplicit1: Timer[F] = timer

    lazy val cronScheduler: Scheduler[F, CronExpr] = Cron4sScheduler.systemDefault[F]
    lazy val eachFiveMinutes = Cron.unsafeParse("0 */30 * ? * *")
    lazy val streamsSystemDefault: ScheduledStreams[F, CronExpr] =
      new ScheduledStreams(cronScheduler)


    streamsSystemDefault
      .awakeEvery(eachFiveMinutes)
      .evalMap { _ =>
        Sync[F].delay(println("cron")) >>
          subscriptionsR
            .get
            .flatMap { subscriptions =>
              subscriptions.value.values.flatMap(_.toList).toSet.toList
                .traverse_{ chat: EventModel.ChatId =>
                  Sync[F].delay(println(chat)) >>
                    BotDsl[F].sendMessage(chat.unwrap, "alive - " + chat.show)
                }
            }

      }


  }


}
