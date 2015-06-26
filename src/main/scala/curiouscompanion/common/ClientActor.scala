package curiouscompanion.common

import akka.actor._

import akka.event.LoggingReceive
import curiouscompanion.model.{ClientMessage,ClientProtocol,Notification}
import ClientProtocol._
import akka.actor.Terminated

class ClientActor(notifiers:Map[String, ActorRef]) extends Actor with akka.actor.ActorLogging {

  var subscribers = Map.empty[String, Set[ActorRef]]
  var topic: String = "None"
  def receive: Receive = LoggingReceive {
    case NewParticipant(name, subscriber) =>
      context.watch(subscriber)
      //get a set of subscribers or empty set
      var refs = subscribers getOrElse (name, Set.empty[ActorRef])
     topic=name
      //concat new subscriber
      refs += subscriber
      //add to the map (topic, updated set)
      subscribers += (name -> refs)
      //subscribe to NotificationActor
      
      sendAdminMessage(s"$name joined!")
    case msg: ReceivedMessage => dispatch(msg.toClientMessage)
    case msg: ClientMessage => dispatch(msg)
    case ParticipantLeft(person) => sendAdminMessage(s"$person left!")
    case Terminated(sub) => 
      subscribers.foreach((node)=> node._2 - sub)
      //unsubscribe to NotificationActor
    case not@Notification(topic: String, message: String, effect: String, time:Long) =>
      subscribers(topic).foreach(_ ! not)

  }
  def getTopic(): String = topic
  def sendAdminMessage(msg: String): Unit = dispatchAll(ClientMessage("admin", msg))
  def dispatchAll(msg: ClientMessage): Unit = subscribers.foreach(_._2 .foreach(_ ! msg))
  def dispatch(msg: ClientMessage): Unit = subscribers(this.topic).foreach(_ ! msg)
}