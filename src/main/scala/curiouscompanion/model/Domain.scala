package curiouscompanion.model

import akka.actor.ActorRef

case class Notifier(topic: String, subscribers: Set[ActorRef])

case class ClientMessage(sender: String, message: String)
case class Notification(topic: String, message: String, location: String, effect: String, time: Long)

object NotifierProtocol {
  sealed trait NotifierEvent
  
 
  case class RequestEvent(ref: ActorRef) extends NotifierEvent
  case class SubscribeEvent(subscriber: ActorRef) extends NotifierEvent
  case class UnsubscribeEvent(subscriber: ActorRef) extends NotifierEvent
   case class NextEvent() extends NotifierEvent
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