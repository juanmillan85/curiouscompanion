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
import scala.concurrent._

class NotifierActor(topic: String) extends Actor {
  import system.dispatcher
  import curiouscompanion.repository._
  implicit val system = context.system
  object repo extends AccountRepositoryRedis

  val random = new scala.util.Random()

  var subscribers = Set.empty[ActorRef]
  /*
  var typeMessages = List("error", "warning", "success", "info")
  var positions = List("toast-top-right", "toast-bottom-right", "toast-bottom-left") //, "toast-top-center", "toast-bottom-center")
  */
  val log = Logging(context.system, this)
  //random 
  var cont = 0
  override def preStart() {
    val that = self
    //import context.dispatcher
    /* val start = context.system.scheduler.scheduleOnce(5.seconds) {
      that ! NextEvent

    }*/
  }

  var currentState: Notifier = Notifier(topic, subscribers)

  def receive: Receive = LoggingReceive {
    case NextEvent => {
      val sec = random.nextInt(30) + 4
      val delay = random.nextInt(5000) + 3000
      cont += 1

      //log.debug(topic + s":$cont")
      var f: Future[Notification] = repo.get(topic + s":$cont")

      f onSuccess {
        case n: Notification =>
          //println(n)
         // log.debug(subscribers.size.toString())
          subscribers
            .foreach { x =>
              //log.debug("Send to:" + topic)
              x ! ClientMessage(topic, "{\"topic\": \"" + topic +
                "\",\"message\": \"" + n.message + "\",\"location\": \"" + n.location +
                "\",\"type\": \"" + n.optionType +
                "\",\"keyword\": \"" + n.keywords +
                "\",\"created\": \"" + new Date().toString + "\",\"time\": " + delay + "}")
            }
      }
      var id=s"$topic-not"
      repo.count(id) onSuccess {
        case res:Long=>
     //     println(s"$cont> $res")
        if (cont >=res) {
          cont = 0;
          
        }
      }

      //subrcibers
      if (!subscribers.isEmpty) {
        val that = self
        //import context.dispatcher
        val next = context.system.scheduler.scheduleOnce(Duration(sec, SECONDS))(that ! NextEvent)
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
    case Store =>

  }
}