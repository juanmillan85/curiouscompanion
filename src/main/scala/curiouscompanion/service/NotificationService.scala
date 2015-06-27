package curiouscompanion.service

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import curiouscompanion.common.NotifierActor

class NotificationService(implicit system: ActorSystem) {
 

  var topics: List[String] = List("General", "Financial", "Sports", "Fashion")
  var notificationRouter: Map[String, ActorRef] = Map.empty[String, ActorRef]
  topics.foreach((topic: String) => {

    var ref: ActorRef = system.actorOf(Props(new NotifierActor(topic)),
      name = topic)
    notificationRouter += (topic -> ref)
  })
  def getNotifiers(): Map[String, ActorRef] = {
    return notificationRouter
  }

}