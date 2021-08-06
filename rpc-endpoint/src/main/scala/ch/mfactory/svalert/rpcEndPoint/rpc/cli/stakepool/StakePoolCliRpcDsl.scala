package ch.mfactory.svalert.rpcEndPoint.rpc.cli.stakepool

import cats.effect.Concurrent
import ch.mfactory.svalert.shared.model.rpc.cli.SolanaStakePool._
import ch.mfactory.svalert.shared.shell.CliDsl
import cats.implicits._

trait StakePoolCliRpcDsl[F[_]] {
  def createPool(): F[StakePool]
}

object StakePoolCliRpcDsl {

  def apply[F[_]](implicit ev: StakePoolCliRpcDsl[F]): StakePoolCliRpcDsl[F] = ev

  def interpreter[
    F[_] : CliDsl : Concurrent
  ]: StakePoolCliRpcDsl[F] = new StakePoolCliRpcDsl[F] {

    lazy val command = "spl-stake-pool create-pool --fee-numerator 3 --fee-denominator 100 --max-validators 1000"

    lazy val reserveAccountPattern = "creating reserve stake"
    lazy val mintAccountPattern = "Creating mint"
    lazy val poolFeeCollectionAccountPattern = "Creating pool fee collection account"
    lazy val stakePoolWithdrawAuthorityPattern = "Stake pool withdraw authority"
    lazy val stakePoolAccount = "Creating stake pool"

    lazy val patterns = List(reserveAccountPattern,
      mintAccountPattern, poolFeeCollectionAccountPattern,
      stakePoolWithdrawAuthorityPattern, stakePoolAccount)


    private def getAccountByPattern(data: List[String], pattern: String): Option[String] =
      data.find(_.contains(pattern)).map(_.replace(pattern, "").trim)

    override def createPool(): F[StakePool] = {
      CliDsl[F]
        .execute(command)
        .map{ result =>


          result.toLowerCase.split("\n").toList.map{ line =>
            patterns.find(line.contains(_)).map(line.replace(_, "").trim)
          }.collect{case Some(x) => x}

        }


    }
  }


}
