package ch.mfactory.svalert.kafkaProxyEndpoint

import cats.effect.Sync
import mfactory.ch.buildinfo.BuildInfo
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication
import cats.implicits._
import ch.mfactory.svalert.shared.config.SharedConfig.BasicCredentials
import org.http4s

object BasicAuth {

  def apply[
    F[_]: Sync
  ](basicCredentials: BasicCredentials): AuthMiddleware[F, String] =
  {
    authentication.BasicAuth(BuildInfo.name, (value: http4s.BasicCredentials) => {
      val result = (
        if ((value.password === basicCredentials.password) && (value.username === basicCredentials.user)) {
          Sync[F].delay(println(s"success username: ${value.username}")) >>
          Option("ok").pure[F]
        } else {
          Sync[F].delay(println(s"failed username: ${value.username}")) >>
          Option.empty[String].pure[F]
        })

      result
    })
  }

}
