package ch.mfactory.svalert.shared.model.rpc.cli

object SolanaStakePool {

  final case class StakePool(reserveStakeAccount: String,
                             mintAccount: String,
                             poolFeeCollectionAccount: String,
                             stakePoolWithdrawAuthority: String,
                             stakePool: String)

}
