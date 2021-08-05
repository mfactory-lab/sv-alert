package ch.mfactory.svalert.kafkaProxyEndpoint

import cats.effect._
import ch.mfactory.svalert.shared.model.Domain._
import org.http4s.circe._
import org.http4s.rho.swagger.SwaggerSyntax
import org.http4s.rho.{RhoRoutes, http4sLiteralsSyntax}
import org.http4s._
import cats.implicits._
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import ch.mfactory.svalert.shared.model.events.EventModel.EmptyKey
import ch.mfactory.svalert.shared.model.events.Notifications._
import io.circe.Decoder.Result
import io.circe.ParsingFailure
import io.circe.syntax._
import io.circe.parser._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class Routes[
  F[+_] : Async : JsonProducerDsl
](swaggerSyntax: SwaggerSyntax[F])
  extends RhoRoutes[F] with CirceEntityEncoder with CirceEntityDecoder
{

  import swaggerSyntax._

  lazy val api = "api"

  "We don't want to have a real 'root' route anyway... " **
    GET |>> TemporaryRedirect(uri"/swagger-ui")

  "This route receives a DelegatorCommissionChangeEvent" **
    List("samples", api) @@
      POST / api / "commission-change" ^ EntityDecoder[F, DelegatorCommissionChangeEvent] |>>
    { event: DelegatorCommissionChangeEvent =>

      JsonProducerDsl[F]
        .produce(EmptyKey, event)
        .flatMap(_ => Ok("you posted" + event.asJson.noSpaces))


    }

  "This route receives a KapacitorNotification " **
    List("notifications", api) @@
      POST / api / "kapacitor-notification" ^ EntityDecoder[F, KapacitorEvent]  |>>
    { event: KapacitorEvent =>

      for {
        message <- parse(event.message)
                    .leftMap(e => new Exception(e.message))
                    .flatMap(_.as[KapacitorNotification]
                      .leftMap(e => new Exception(e.getLocalizedMessage))
                    )
                    .fold(
                      _.raiseError[F, KapacitorNotification],
                      _.pure[F]
                    )
        _ <- JsonProducerDsl[F].produce(EmptyKey, message)
        response <- Ok("you posted" + message.asJson.noSpaces)
      } yield response


    }

}
