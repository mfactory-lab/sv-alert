package ch.mfactory.svalert.shared.services

import cats.effect.{Sync, Timer}
import fs2.Stream
import cats.implicits._
import scala.concurrent.duration.DurationInt

object Common {

  def startBackgroundService[
    F[_]: Sync
  ](timer: Timer[F])(service: F[Unit]): Stream[F, Unit] = {

    implicit val badImplicit: Timer[F] = timer
    Stream.retry(
      service,
      5.seconds,
      _ => 5.seconds,
      Int.MaxValue,
      _ => true
    )

//
//    lazy val stream: Stream[F, Unit] = Stream
//      .eval(service)
//      .mask
//      .handleErrorWith{ t =>
//        Stream.eval(
//          Sync[F].delay(println(t.getLocalizedMessage)) >>
//            timer.sleep(5.seconds)
//        ).append(stream)
//      }
//
//    stream
  }

}
