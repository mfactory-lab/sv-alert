package ch.mfactory.svalert.shared.config

import cats.effect.{Blocker, ContextShift, Sync}
import com.typesafe.config.ConfigFactory
import pureconfig.{ConfigReader, ConfigSource}
import cats.implicits._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

import scala.reflect.ClassTag

object ConfigService {

  lazy val defaultConfigName = "application.conf"

  def produceConfig[
    F[_] : Sync,
    A: ClassTag: ConfigReader
  ](configName: String, blocker: Blocker, cs: ContextShift[F]): F[A] = {
    implicit val badImplicit: ContextShift[F] = cs
    for {
      config <- Sync[F].delay(ConfigFactory.load(configName))
      result <- ConfigSource.fromConfig(config).loadF[F, A](blocker)
    } yield result

  }

}
