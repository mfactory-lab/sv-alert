package ch.mfactory.svalert.rpcEndPoint.rpc.cli.json

import cats.effect.Concurrent
import cats.implicits._
import ch.mfactory.svalert.shared.shell.CliDsl
import io.circe
import io.circe.Decoder
import io.circe.parser._

trait JsonCliRpcDsl[F[_]] {

  def execute[X: Decoder](command: String): F[X]

}

object JsonCliRpcDsl {

  lazy val prefix = ""
//  lazy val prefix = "/home/demiourgos728/.local/share/solana/install/active_release/bin/"

  lazy val suffix = " -u http://157.90.177.36:8899"

  def apply[F[_]](implicit ev: JsonCliRpcDsl[F]): JsonCliRpcDsl[F] = ev

  def interpreter[
    F[_] : CliDsl :  Concurrent
  ](): JsonCliRpcDsl[F] = new JsonCliRpcDsl[F]
  {
    override def execute[X: Decoder](command: String): F[X] = {
//      val command = "solana validators --output json"
      CliDsl[F]
        .execute(prefix + command + suffix)
        .flatMap{ value =>
          println(value)
          decode[X](value)
            .fold[F[X]](
              (error: circe.Error) => (new Exception(error.show)).raiseError[F, X],
              (v: X) => v.pure[F])
        }

    }
  }
}
