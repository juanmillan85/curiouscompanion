package curiouscompanion.common

import akka.actor._
import akka.event.LoggingReceive
import curiouscompanion.model.{ NotifierProtocol, Notification, Notifier}
import NotifierProtocol._
 
class NotifierActor(topic: String,
  subscribers: List[ActorRef]) extends Actor with akka.actor.ActorLogging {
 

  var currentState: Notifier = Notifier(topic, subscribers)
  
  def receive: Receive = LoggingReceive {
    case not: Notification =>
    case sub: SubscriptionEvent =>
    case unsub: UnsubscribeEvent =>
  }
}