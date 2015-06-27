package curiouscompanion.app

import curiouscompanion.service._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import scala.util.{ Success, Failure }
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow

object Boot extends App {
  implicit val system = ActorSystem("curiosify")
  import system.dispatcher
  //A ActorFlowMaterializer takes the list of transformations comprising a akka.stream.scaladsl.Flow
  //and materializes them in the form of org.reactivestreams.Processor instances. How transformation 
  //steps are split up into asynchronous regions is implementation dependent.
  implicit val materializer = ActorFlowMaterializer()

  val config = system.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")
  val notiService= new NotificationService
  implicit val notifiers=notiService.getNotifiers
  val service = new Webservice

  val binding = Http().bindAndHandle(service.route, interface, port)
  binding.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
      system.shutdown()
  }
}
