package ch.mfactory.svalert.model


object Model {

  sealed trait ChatState

  final case object Subscribed extends ChatState
  final case object Unsubscribed extends ChatState


}
