package ch.mfactory.svalert.rpcEndPoint

import cats.effect.{Blocker, ConcurrentEffect, ContextShift}
import ch.mfactory.svalert.rpcEndPoint.rpc.cli.CliRpcDsl
import ch.mfactory.svalert.shared.shell.CliDsl

trait Interpreters[F[_]] {
  implicit def shellDsl: CliDsl[F]
  implicit def shellRpcDsl: CliRpcDsl[F]
}

object Interpreters {

  def apply[
    F[_]: ConcurrentEffect
  ](cs: ContextShift[F], blocker: Blocker): Interpreters[F] = new Interpreters[F]
  {

    override implicit def shellDsl: CliDsl[F] = CliDsl.interpreter(cs, blocker)

    override implicit def shellRpcDsl: CliRpcDsl[F] = CliRpcDsl.interpreter()
  }

}
