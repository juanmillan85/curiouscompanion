package curiouscompanion.common

import akka.actor._

import akka.event.LoggingReceive
import akka.event.Logging
import curiouscompanion.model.{ ClientMessage, ClientProtocol, Notification, NotifierProtocol }
import ClientProtocol._
import NotifierProtocol._
import akka.actor.Terminated

class ClientActor(notifiers: Map[String, ActorRef]) extends Actor {
   
  val log = Logging(context.system, this)
  
  var query: String = ""
  var subscribers = Map.empty[String, Set[ActorRef]]
  
  var topic: String = "None"
  def receive: Receive = LoggingReceive {
    case NewParticipant(name, subscriber) =>
      context.watch(subscriber)
      //get a set of subscribers or empty set
      var refs = subscribers getOrElse (name, Set.empty[ActorRef])
      topic = name
      //concat new subscriber
      refs += subscriber
      //add to the map (topic, updated set)
      subscribers += (name -> refs)
      //subscribe to NotificationActor
      log.debug("Sub"+subscriber.toString())
      
      notifiers(topic)! SubscribeEvent(subscriber)
      sendAdminMessage(s"$name joined!")
    case msg: ReceivedMessage    => dispatch(msg.toClientMessage)
    case msg: ClientMessage      => dispatchAll(msg)
    case ParticipantLeft(person) => sendAdminMessage(s"$person left!")
    case Terminated(sub) =>
      
      subscribers.foreach((node) => node._2 - sub)
      notifiers(topic)! UnsubscribeEvent(sub)
    //unsubscribe to NotificationActor
    case not @ Notification(topic: String, message: String, location: String, effect: String, time: Long) =>
     // subscribers(topic).foreach(_ ! not)
      dispatch(new ClientMessage(topic ,"{topic:"+topic+",message:"+message+",location:"+location+",effect:"+effect+",time:"+time+"}"))
  }

  
  def getTopic(): String = topic
  def sendAdminMessage(msg: String): Unit = dispatchAll(ClientMessage("admin", msg))
  def dispatchAll(msg: ClientMessage): Unit = subscribers.foreach(_._2.foreach(_ ! msg))
  def dispatch( msg:ClientMessage): Unit = subscribers(msg.sender).foreach(_ ! msg)
}