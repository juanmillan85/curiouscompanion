package curiouscompanion.common

import akka.actor.ActorRef
import akka.actor.Actor
import akka.event.Logging
import akka.event.LoggingReceive
import java.util.Date
import curiouscompanion.model.{ ClientMessage, NotifierProtocol, Notification, Notifier, ClientProtocol }
import ClientProtocol._
import NotifierProtocol._
import scala.concurrent.duration._

class NotifierActor(topic: String) extends Actor {
  val random = new scala.util.Random()

  var subscribers = Set.empty[ActorRef]
  var messages = List("MSG1", "MSG2", "MSG3", "MSG4", "MSG5", "MSG6", "MSG7", "MSG9", "MSG10")
  var positions = List("bottom", "left", "right", "top-right", "top-center", "top-left")
  // log
  val log = Logging(context.system, this)
  //random 
  var cont = 0
  override def preStart() {
    val that = self
    import context.dispatcher
    context.system.scheduler.scheduleOnce(5.seconds)(that ! NextEvent)
  }

  var currentState: Notifier = Notifier(topic, subscribers)

  def receive: Receive = LoggingReceive {
    case NextEvent => {
      var sec = random.nextInt(15) + 1
      var msg = cont % messages.size //random.nextInt(messages.size)
      cont += 1
      var pos = random.nextInt(positions.size)

      var delay = random.nextInt(10000)

      log.debug(subscribers.size.toString())
      subscribers
        .foreach { x =>
          log.debug("Send to:" + topic)
          x ! ClientMessage(topic, "{topic:" + topic +
            ",message:" + messages(msg) + ",location:" + positions(pos) +
            ",effect:" + new Date().toString + ",time:" + delay + "}")
        }

      //subrcibers
      if (!subscribers.isEmpty) {
        val that = self
        import context.dispatcher
        context.system.scheduler.scheduleOnce(Duration(sec, SECONDS))(that ! NextEvent)
      }

    }
    case SubscribeEvent(ref) => {
      if (subscribers.isEmpty)
        self ! NextEvent
      subscribers += ref
      //subscribers = ref
      currentState = currentState.copy(subscribers = subscribers)

    }

    case UnsubscribeEvent(ref) =>
      subscribers -= ref
  }
}