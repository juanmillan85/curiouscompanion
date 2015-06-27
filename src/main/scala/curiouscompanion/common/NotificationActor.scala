package curiouscompanion.common

import akka.actor.ActorRef
import akka.actor.Actor
import akka.event.Logging
import akka.event.LoggingReceive
import curiouscompanion.model.{ NotifierProtocol, Notification, Notifier, ClientProtocol }
import ClientProtocol._
import NotifierProtocol._
import scala.concurrent.duration._

class NotifierActor(topic: String) extends Actor {
  var subscribers = Set.empty[ActorRef]
  val log = Logging(context.system, this)

  override def preStart() {
    val that = self
    import context.dispatcher
    context.system.scheduler.scheduleOnce(5.seconds)(that ! NextEvent)
  }

  // var currentState: Notifier = Notifier(topic, subscribers)

  def receive: Receive = LoggingReceive {
    case  NextEvent  => {
      subscribers
        .foreach { x =>
          log.debug(x.toString())
          x ! Notification("General", "MSG", "Location", "Funy", 1000l)
        }
      if (!subscribers.isEmpty) {
        val that = self
        import context.dispatcher
        context.system.scheduler.scheduleOnce(5.seconds)(that ! NextEvent)
      }

    }
    case SubscribeEvent(ref) => {
      subscribers += ref
      //subscribers = ref
      log.debug(subscribers.size.toString())
      self ! NextEvent
      //currentState = currentState.copy(subscribers = )
    }

    case unsub: UnsubscribeEvent =>
  }
}