package curiouscompanion.model

import akka.actor.ActorRef

case class Notifier(topic: String, subscribers: Set[ActorRef])
sealed trait Message
case class ClientMessage(sender: String, message: String) extends Message {
  override def toString(): String = s"($sender,$message)";
}
case class Notification(id: String, topic: String, message: String, keywords: String, optionType: String, location: String, effect: String, time: Long) extends Message {
  override def toString(): String = s"($id,$message,$keywords,$optionType,$location,$time)";

}

object NotifierProtocol {
  sealed trait NotifierEvent

  case class RequestEvent(ref: ActorRef) extends NotifierEvent
  case class SubscribeEvent(subscriber: ActorRef) extends NotifierEvent
  case class UnsubscribeEvent(subscriber: ActorRef) extends NotifierEvent
  case class NextEvent() extends NotifierEvent
  case class Store(not: Notification) extends NotifierEvent
  case class Get(id: String)
  case class Delete(id: String) extends NotifierEvent
  final case object OK
}

object ClientProtocol {
  sealed trait ClientEvent

  case class NewParticipant(name: String, subscriber: ActorRef) extends ClientEvent
  case class ParticipantLeft(name: String) extends ClientEvent
  case class ReceivedMessage(sender: String, message: String) extends ClientEvent {
    def toClientMessage: ClientMessage = ClientMessage(sender, message)
  }
}
