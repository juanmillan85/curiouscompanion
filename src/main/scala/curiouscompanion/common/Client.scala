package curiouscompanion.common

/**
 * @author JuanDavid
 */

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import akka.event.LoggingReceive

//client curious companion
import curiouscompanion.model.ClientProtocol._
import curiouscompanion.model._



trait Client {
  def clientFlow(sender: String): Flow[String, ClientMessage, Unit]

  def injectMessage(message: ClientMessage): Unit

}

object Client {

  def create(system: ActorSystem, notifiers:Map[String,ActorRef]): Client = {
    // The implementation uses a single actor per Client to collect and distribute
    // Client messages. It would be nicer if this could be built by stream operations
    // directly.
    val ClientActor =
      system.actorOf(Props(new ClientActor(notifiers)))

    // Wraps the ClientActor in a sink. When the stream to this sink will be completed
    // it sends the `ParticipantLeft` message to the ClientActor.
    // FIXME: here some rate-limiting should be applied to prevent single users flooding the Client
    def ClientInSink(sender: String) = Sink.actorRef[ClientEvent](ClientActor, ParticipantLeft(sender))

    // The counter-part which is a source that will create a target ActorRef per
    // materialization where the ClientActor will send its messages to.
    // This source will only buffer one element and will fail if the client doesn't read
    // messages fast enough.
    val ClientOutSource = Source.actorRef[ClientMessage](8, OverflowStrategy.fail)

    new Client {
      def clientFlow(sender: String): Flow[String, ClientMessage, Unit] =
        Flow(ClientInSink(sender), ClientOutSource)(Keep.right) { implicit b =>
          (ClientActorIn, ClientActorOut) =>
            import akka.stream.scaladsl.FlowGraph.Implicits._
            val enveloper = b.add(Flow[String].map(ReceivedMessage(sender, _))) // put the message in an envelope
            val merge = b.add(Merge[ClientEvent](2))

            // the main flow
            enveloper ~> merge.in(0)

            // a side branch of the graph that sends the ActorRef of the listening actor
            // to the ClientActor
            b.materializedValue ~> Flow[ActorRef].map(NewParticipant(sender, _)) ~> merge.in(1)

            // send the output of the merge to the ClientActor
            merge ~> ClientActorIn

            (enveloper.inlet, ClientActorOut.outlet)
        }.mapMaterializedValue(_ ⇒ ())
      def injectMessage(message: ClientMessage): Unit = ClientActor ! message // non-streams interface
    }
  }

  
}