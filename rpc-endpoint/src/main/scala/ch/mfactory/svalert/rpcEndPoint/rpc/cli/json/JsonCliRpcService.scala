package ch.mfactory.svalert.rpcEndPoint.rpc.cli.json

import cats.MonadError
import ch.mfactory.svalert.shared.model.rpc.cli.SolanaStakeAccount.StakeAccount
import ch.mfactory.svalert.shared.model.rpc.cli.SolanaValidators.Validators
import cats.implicits._

object JsonCliRpcService {

  def solanaValidators[F[_] : JsonCliRpcDsl](): F[Validators] =
    JsonCliRpcDsl[F].execute("solana validators --output json")

  def stakeAccount[F[_] : JsonCliRpcDsl](stakeAccount: String): F[StakeAccount] =
    JsonCliRpcDsl[F].execute(s"solana stake-account $stakeAccount --with-rewards --num-rewards-epochs=10 --output json")

  def test[F[_] : JsonCliRpcDsl : MonadError[*[_], Throwable]](): F[Validators] =
    JsonCliRpcDsl[F]
      .execute[Validators]("ls -lah")
      .handleErrorWith { e: Throwable =>
        println(e.getLocalizedMessage)

        Validators(0, 0, 0, List(), Map()).pure[F]
      }

}
