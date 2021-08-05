package ch.mfactory.svalert.shared.model.rpc.cli

import cats.Show
import io.circe._
import io.circe.generic.semiauto._

object SolanaValidators {

  final case class Validators(totalActiveStake: BigInt,
                                         totalCurrentStake: BigInt,
                                         totalDelinquentStake: BigInt,
                                         validators: List[Validator],
                                         stakeByVersion: Map[String, StakeByVersion])

  implicit val show: Show[Validators] = Show.fromToString

  final case class Validator(identityPubkey: String,
                             voteAccountPubkey: String,
                             commission: Int,
                             lastVote: Int,
                             rootSlot: Int,
                             credits: Int,
                             epochCredits: Int,
                             activatedStake: BigInt,
                             version: String,
                             delinquent: Boolean,
                             skipRate: Float
                            )

  final case class Version(unwrap: String)

  final case class StakeByVersion(currentValidators: Int,
                                  delinquentValidators: Int,
                                  currentActiveStake: BigInt,
                                  delinquentActiveStake: BigInt)


  implicit val stakeByVersionDecoder: Decoder[StakeByVersion] = deriveDecoder
  implicit val stakeByVersionEncoder: Encoder[StakeByVersion] = deriveEncoder

  implicit val versionDecoder: Decoder[Version] = deriveDecoder
  implicit val versionEncoder: Encoder[Version] = deriveEncoder

  implicit val validatorDecoder: Decoder[Validator] = deriveDecoder
  implicit val validatorEncoder: Encoder[Validator] = deriveEncoder

  implicit val validatorsDecoder: Decoder[Validators] = deriveDecoder
  implicit val validatorsEncoder: Encoder[Validators] = deriveEncoder
}
