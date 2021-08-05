package ch.mfactory.svalert.rpcEndPoint.rpc.cli

import cats.{MonadError}
import ch.mfactory.svalert.shared.model.rpc.cli.SolanaStakeAccount.StakeAccount
import ch.mfactory.svalert.shared.model.rpc.cli.SolanaValidators.Validators
import cats.implicits._

object CliRpcService {

  def solanaValidators[F[_] : CliRpcDsl](): F[Validators] =
    CliRpcDsl[F].execute("solana validators --output json")

  def stakeAccount[F[_] : CliRpcDsl](stakeAccount: String): F[StakeAccount] =
    CliRpcDsl[F].execute(s"solana stake-account $stakeAccount --with-rewards --num-rewards-epochs=10 --output json")

  def test[F[_] : CliRpcDsl : MonadError[*[_], Throwable]](): F[Validators] =
    CliRpcDsl[F]
      .execute[Validators]("ls -lah")
      .handleErrorWith{ e: Throwable =>
        println(e.getLocalizedMessage)

        Validators(0, 0, 0, List(), Map()).pure[F]
      }

}
