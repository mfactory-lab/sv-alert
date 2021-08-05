package ch.mfactory.svalert.shared.model.rpc.cli

import io.circe._
import io.circe.generic.semiauto._

object SolanaStakeAccount {

  final case class StakeAccount(stakeType: String,
                                accountBalance: BigInt,
                                creditsObserved: BigInt,
                                delegatedStake: BigInt,
                                delegatedVoteAccountAddress: String,
                                activationEpoch: Int,
                                staker: String,
                                withdrawer: String,
                                rentExemptReserve: BigInt,
                                activeStake: BigInt,
                                epochRewards: List[EpochRewards])

  final case class EpochRewards(epoch: Int,
                                effectiveSlot: Int,
                                amount: BigInt,
                                postBalance: BigInt,
                                percentChange: Float,
                                apr: Float)

  implicit val epochRewardsDecoder: Decoder[EpochRewards] = deriveDecoder
  implicit val epochRewardsEncoder: Encoder[EpochRewards] = deriveEncoder

  implicit val stakeAccountDecoder: Decoder[StakeAccount] = deriveDecoder
  implicit val stakeAccountEncoder: Encoder[StakeAccount] = deriveEncoder

}
