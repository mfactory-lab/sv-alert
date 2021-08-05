package ch.mfactory.svalert.shared.rest.client

trait RestClientDsl[F[_]] {

}

object RestClientDsl {

  def apply[F[_]](implicit ev: RestClientDsl[F]): RestClientDsl[F] = ev

}
