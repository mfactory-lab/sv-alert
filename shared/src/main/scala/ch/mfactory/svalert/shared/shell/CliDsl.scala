package ch.mfactory.svalert.shared.shell

import cats.effect.{Blocker, ConcurrentEffect, ContextShift}

trait CliDsl[F[_]] {

  def execute(command: String): F[String]

}

object CliDsl {


  def apply[F[_]](implicit ev: CliDsl[F]): CliDsl[F] = ev

  def interpreter[
    F[_] : ConcurrentEffect
  ](cs: ContextShift[F], blocker: Blocker): CliDsl[F] = new CliDsl[F] {

    override def execute(command: String): F[String] = {

      import sys.process._

      implicit val badImplicit0: ContextShift[F] = cs

      blocker.delay(command.!!)
    }
  }

}
