package ch.mfactory.svalert.shared.model.events

import io.circe._
import io.circe.generic.semiauto._

object Notifications {

  final case class KapacitorNotification(id: String, level: String, value: String)

  final case class KapacitorEvent(message: String)

  implicit lazy val kapacitorNotificationDecoder: Decoder[KapacitorNotification] = deriveDecoder
  implicit lazy val kapacitorNotificationEncoder: Encoder[KapacitorNotification] = deriveEncoder

  implicit lazy val kapacitorEventDecoder: Decoder[KapacitorEvent] = deriveDecoder
  implicit lazy val kapacitorEventEncoder: Encoder[KapacitorEvent] = deriveEncoder

}
