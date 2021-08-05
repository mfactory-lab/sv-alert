package ch.mfactory.svalert.kafkaProxyEndpoint

import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import com.http4s.rho.swagger.ui.SwaggerUi
import org.http4s.implicits._
import org.http4s.rho.swagger.{SwaggerMetadata, SwaggerSyntax}
import org.http4s.rho.swagger.models.{Info, Tag}
import org.http4s.rho.swagger.syntax.{io => ioSwagger}
import org.http4s.server.blaze.BlazeServerBuilder
import org.log4s.getLogger
import cats.implicits._
import ch.mfactory.svalert.kafkaProxyEndpoint.config.Config
import ch.mfactory.svalert.shared.config.ConfigService
import ch.mfactory.svalert.shared.kafka.producer.JsonProducerDsl
import mfactory.ch.buildinfo.BuildInfo
import org.http4s.Method.GET
import org.http4s.rho.RhoMiddleware
import org.http4s.server.middleware.Logger
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, AuthedService, HttpRoutes, Request, Response}

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  private val logger = getLogger


  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use { blocker =>

      val ec = blocker.blockingContext
      val cs = IO.contextShift(ec)
      val timer = IO.timer(ec)

      runF[IO](blocker, cs, timer, ioSwagger, ec)

    }

  def runF[
    F[+_] : ConcurrentEffect
  ](blocker: Blocker,
    cs: ContextShift[F],
    timer: Timer[F],
    swaggerSyntax: SwaggerSyntax[F],
    ec: ExecutionContext): F[ExitCode] =
  {
    ConfigService.produceConfig[F, Config](ConfigService.defaultConfigName, blocker, cs).flatMap { config =>

      val resources = for {
        producer <- JsonProducerDsl.createProducer(config.producer, cs)
        partitionCount <- Resource.eval(JsonProducerDsl.partitionCount(config.producer, cs))
      } yield (producer, partitionCount)

      resources.use { case (producer, partitionCount) =>
        val interpreters = Interpreters[F](config.producer, producer, partitionCount)
        import interpreters._
        runApplicationF[F](config, blocker, cs, timer, swaggerSyntax, ec)
      }
    }
  }

  def runApplicationF[
    F[+_] : ConcurrentEffect : JsonProducerDsl
  ](config: Config,
    blocker: Blocker,
    cs: ContextShift[F],
    timer: Timer[F],
    swaggerSyntax: SwaggerSyntax[F],
    ec: ExecutionContext): F[ExitCode] =
  {
    implicit val badImplicit1: ContextShift[F] = cs
    implicit val badImplicit2: Timer[F] = timer

    val metadata = SwaggerMetadata(
      apiInfo = Info(title = "Kafka Rest Proxy Endpoint", version = BuildInfo.version)
    )

    logger.info(s"Starting Kafka Rest Proxy Endpoint on '${config.http.port.toString}'")

    @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
    val swaggerUiRhoMiddleware: RhoMiddleware[F] = SwaggerUi[F].createRhoMiddleware(blocker, swaggerMetadata = metadata)

    val myRoutes: HttpRoutes[F] = new Routes[F](swaggerSyntax).toRoutes(swaggerUiRhoMiddleware)

    val auth: AuthMiddleware[F, String] = BasicAuth[F](config.http.credentials)

    val httpApp: Kleisli[F, Request[F], Response[F]] =
      Logger.httpApp(logHeaders = true, logBody = true)(
        auth(myRoutes.local(_.req)).orNotFound
      )

    BlazeServerBuilder[F](ec)
      .withHttpApp(httpApp)
      .bindHttp(config.http.port, config.http.host)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
