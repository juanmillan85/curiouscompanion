package curiouscompanion.model

import akka.actor.ActorRef

case class Notifier (topic: String,subscribers: Seq[ActorRef])
case class ClientMessage(sender: String, message: String)
case class Notification(topic: String, message: String, effect: String, time:Long)

object NotifierProtocol {
  sealed trait NotifierEvent
  
  final case class  RequestEvent(sender: ActorRef) 		extends NotifierEvent  
  final case class  SubscriptionEvent(sender:ActorRef) 	extends NotifierEvent
  final case class  UnsubscribeEvent (sender: ActorRef) extends NotifierEvent
  final case object OK
}

object ClientProtocol{
  sealed trait ClientEvent
  
  case class NewParticipant(name: String, subscriber: ActorRef) extends ClientEvent
  case class ParticipantLeft(name: String) extends ClientEvent
  case class ReceivedMessage(sender: String, message: String) extends ClientEvent {
    def toClientMessage: ClientMessage = ClientMessage(sender, message)
  }
}