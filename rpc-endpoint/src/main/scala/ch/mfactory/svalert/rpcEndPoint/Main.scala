package ch.mfactory.svalert.rpcEndPoint

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import ch.mfactory.svalert.rpcEndPoint.config.Config
import ch.mfactory.svalert.shared.config.ConfigService
import com.http4s.rho.swagger.ui.SwaggerUi
import org.http4s.rho.swagger.{SwaggerMetadata, SwaggerSyntax}
import org.http4s.rho.swagger.models.{Info, Tag}
import org.log4s.getLogger
import org.http4s.rho.swagger.syntax.{io => ioSwagger}
import org.http4s.server.blaze.BlazeServerBuilder
import cats.implicits._
import ch.mfactory.svalert.rpcEndPoint.rpc.cli.json.JsonCliRpcDsl
import org.http4s.implicits._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object Main extends IOApp {
  lazy val configName = "application.conf"
  private val logger = getLogger

  def run(args: List[String]): IO[ExitCode] = {

    Blocker[IO].use { blocker1 =>

      val ec = blocker1.blockingContext
      val cs1 = IO.contextShift(ec)
      val timer = IO.timer(ec)

      val cachedThreadPool = Executors.newCachedThreadPool()
      val blocker2: Blocker = Blocker.liftExecutionContext(
        ExecutionContext.fromExecutor(cachedThreadPool)
      )

      val cs2 = IO.contextShift(blocker2.blockingContext)

      cs1.shift >>
      runF[IO](blocker2, cs2, blocker1, cs1, timer, ioSwagger)

    }
  }

  def runF[
    F[+_] : ConcurrentEffect
  ](blocker1: Blocker, cs1: ContextShift[F],
    blocker2: Blocker, cs2: ContextShift[F],
    timer: Timer[F], swaggerSyntax: SwaggerSyntax[F]): F[ExitCode] = {
    ConfigService.produceConfig[F, Config](configName, blocker1, cs1).flatMap { config =>
      val interpreters = Interpreters[F](cs2, blocker2)
      import interpreters._
      runApplication[F](config, blocker1, cs1, timer, swaggerSyntax)
    }
  }

  def runApplication[
    F[+_] : ConcurrentEffect : JsonCliRpcDsl
  ](config: Config, blocker: Blocker, cs: ContextShift[F], timer: Timer[F], swaggerSyntax: SwaggerSyntax[F]): F[ExitCode] = {
    implicit val badImplicit1: ContextShift[F] = cs
    implicit val badImplicit2: Timer[F] = timer

    val metadata = SwaggerMetadata(
      apiInfo = Info(title = "Rho demo", version = "1.2.3"),
      tags = List(Tag(name = "hello", description = Some("These are the hello routes.")))
    )

    logger.info(s"Starting Swagger example on '${config.http.port.toString}'")

    @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
    val swaggerUiRhoMiddleware = SwaggerUi[F].createRhoMiddleware(blocker, swaggerMetadata = metadata)

    val myRoutes = new Routes[F](swaggerSyntax).toRoutes(swaggerUiRhoMiddleware)

    BlazeServerBuilder[F](blocker.blockingContext)
      .withHttpApp(myRoutes.orNotFound)
      .withResponseHeaderTimeout(1.hour)
      .withIdleTimeout(1.hour)
      .bindHttp(config.http.port, config.http.host)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
