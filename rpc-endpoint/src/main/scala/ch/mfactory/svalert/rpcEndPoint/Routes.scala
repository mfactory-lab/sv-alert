package ch.mfactory.svalert.rpcEndPoint

import cats.effect.Async
import ch.mfactory.svalert.rpcEndPoint.rpc.cli.CliRpcService
import org.http4s.circe.{CirceEntityDecoder, CirceEntityEncoder}
import org.http4s.rho.swagger.SwaggerSyntax
import org.http4s.rho.{RhoRoutes, http4sLiteralsSyntax}
import cats.implicits._
import ch.mfactory.svalert.shared.model.rpc.cli.{SolanaStakeAccount, SolanaValidators}
import SolanaValidators._
import ch.mfactory.svalert.rpcEndPoint.rpc.cli.json.{JsonCliRpcDsl, JsonCliRpcService}

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class Routes[
  F[+_] : Async : JsonCliRpcDsl
](swaggerSyntax: SwaggerSyntax[F])
  extends RhoRoutes[F] with CirceEntityEncoder with CirceEntityDecoder
{
  import swaggerSyntax._

  "We don't want to have a real 'root' route anyway... " **
    GET |>> TemporaryRedirect(uri"/swagger-ui")

  "Get Validators Info from cli command 'solana validators'" **
    List("solana", "cli") @@
    GET / "cli" / "validators" |>>
    {
      JsonCliRpcService
        .solanaValidators[F]()
        .map{(x: SolanaValidators.Validators) =>
          println(x.show.take(50))
          Ok(x)
        }

    }

  "Get Stake Account Info from cli command 'solana stake-account {id} --with-rewards --num-rewards-epochs=10 --output json'" **
    List("solana", "cli") @@
      GET / "cli" / "stake-account" / pathVar[String] |>>
    { value: String =>
      JsonCliRpcService
        .stakeAccount[F](value)
        .map((x: SolanaStakeAccount.StakeAccount) => Ok(x))

    }

  "test route " **
    List("test") @@
      GET / "cli" / "test"  |>>
    {
      JsonCliRpcService
        .test[F]()
        .map((x: SolanaValidators.Validators) => Ok(x))

    }

}
