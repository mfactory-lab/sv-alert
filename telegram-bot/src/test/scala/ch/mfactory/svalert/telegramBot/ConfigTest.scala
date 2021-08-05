package ch.mfactory.svalert.telegramBot

import cats.effect.{Blocker, IO}
import ch.mfactory.svalert.shared.config.ConfigService
import ch.mfactory.svalert.telegramBot.config.Config
import org.specs2.ScalaCheck
import org.specs2.matcher.IOMatchers
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext

@SuppressWarnings(Array(
  "org.wartremover.warts.NonUnitStatements",
  "org.wartremover.warts.GlobalExecutionContext"
))
object ConfigTest extends Specification with IOMatchers with ScalaCheck {


  "The config Service" should {
    "produce configs for each config type we know of" in {

      val io: IO[Config] = ConfigService.produceConfig[IO, Config](
        ConfigService.defaultConfigName,
        Blocker.liftExecutionContext(ExecutionContext.global),
        IO.contextShift(ExecutionContext.global)
      )
      io should returnOk
    }
  }

}
