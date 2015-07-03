package curiouscompanion.service

/**
 * @author JuanDavid
 */


import java.util.Date
import curiouscompanion.model._
import curiouscompanion.common._
import akka.actor._
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.stage._
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Sink, Source, Flow }


class Webservice(implicit  fm: FlowMaterializer, system: ActorSystem, notifiers: Map[String, ActorRef]) extends Directives {
  val theChat = Client.create(system,notifiers)
  import system.dispatcher
  system.scheduler.schedule(100.seconds, 100.seconds) {
 
    theChat.injectMessage(ClientMessage(sender = "admin", s"Bling! The time is ${new Date().toString}."))
  }

  def route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("frontend-launcher.js")(getFromResource("web/frontend-launcher.js")) ~
        path("frontend-fastopt.js")(getFromResource("web/frontend-fastopt.js")) ~
        path("chat") {
          parameter('name) { name ⇒
            handleWebsocketMessages(websocketChatFlow(sender = name))
          }
        }
    } ~
      getFromResourceDirectory("web")

  def websocketChatFlow(sender: String): Flow[Message, Message, Unit] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) => msg // unpack incoming WS text messages...
        // This will lose (ignore) messages not received in one chunk (which is
        // unlikely because chat messages are small) but absolutely possible
        // FIXME: We need to handle TextMessage.Streamed as well.
      }
      .via(theChat.clientFlow(sender)) // ... and route them through the chatFlow ...
      .map {
      
      case ClientMessage(sender, message) =>TextMessage.Strict(s"$sender: $message") // ... pack outgoing messages into WS text messages ...
     
      }
      
      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  def reportErrorsFlow[T]: Flow[T, T, Unit] =
    Flow[T]
      .transform(() ⇒ new PushStage[T, T] {
        def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

        override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
          println(s"WS stream failed with $cause")
          super.onUpstreamFailure(cause, ctx)
        }
      })
}
