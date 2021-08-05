package ch.mfactory.svalert.rpcEndPoint.config

import ch.mfactory.svalert.shared.config.ConfigImplicits
import ch.mfactory.svalert.shared.config.SharedConfig._
import pureconfig.generic.semiauto.deriveReader

final case class Config(http: HttpConfig)

object Config extends ConfigImplicits {
  implicit lazy val mainConfigReader = deriveReader[Config]
}
