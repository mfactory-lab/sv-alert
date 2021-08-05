package ch.mfactory.svalert.shared.model

import io.circe._
import io.circe.generic.semiauto._


object Domain {

  final case class ValidatorInfo(identityKey: String, ident: String)

  final case class ValidatorEvent(validatorInfo: ValidatorInfo,
                                  dateTime: String,
                                  votingSkipRate: Float,
                                  delinquent: Boolean)

  final case class Commission(amount: Float,
                              //dateTime: LocalDateTime,
                              epoch: Int)

  final case class DelegatorCommissionChangeEvent(validatorInfo: ValidatorInfo,
                                                  commissions: List[Commission])

  implicit lazy val validatorInfoDecoder: Decoder[ValidatorInfo] = deriveDecoder
  implicit lazy val validatorInfoEncoder: Encoder[ValidatorInfo] = deriveEncoder

  implicit lazy val validatorEventDecoder: Decoder[ValidatorEvent] = deriveDecoder
  implicit lazy val validatorEventEncoder: Encoder[ValidatorEvent] = deriveEncoder

  implicit lazy val commissionEventDecoder: Decoder[Commission] = deriveDecoder
  implicit lazy val commissionEventEncoder: Encoder[Commission] = deriveEncoder

  implicit lazy val delegatorChangeEventDecoder: Decoder[DelegatorCommissionChangeEvent] = deriveDecoder
  implicit lazy val delegatorChangeEventEncoder: Encoder[DelegatorCommissionChangeEvent] = deriveEncoder

}

