package curiouscompanion.repository
import java.util.Date
import curiouscompanion.model._
import scala.concurrent._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.io.{ IO, Tcp }
import akka.util._
import scala.concurrent._
import scala.util.{ Success, Failure }
import scala.concurrent.duration._
import java.util.UUID
import java.net.ServerSocket
import org.scalatest._
import brando._
/**
 * @author Juan
 */
trait NotificationRepository {

  //def query(topic: String): Future[List[Notification]]

  def get(id: String): Future[Notification]
  def store(not: Notification): Future[Notification]
  def count(topic: String): Future[Long]

  //def query(openedOn: Date): Future[Seq[Notification]]
}

class AccountRepositoryRedis(implicit system: ActorSystem) extends NotificationRepository {

  val redis = system.actorOf(Redis("localhost", 6379, 5))
  import system.dispatcher
  Thread.sleep(300)
  //def query(topic: String): Future[List[Notification]] = ???
  def get(id: String): Future[Notification] = {
    implicit val timeout = Timeout(1.seconds)

    val p = Promise[Notification]()
    //println(id)
    var hash = for {
      Response.AsStringsHash(fields) <- redis ? Request("HGETALL", id)
    } yield fields
    hash onComplete {
      case Failure(e) =>
        println("error")
        e.printStackTrace()
      case Success(x: Map[String, String]) =>
       // println(x)
        if (!x.isEmpty)
          p.success(Notification(s"$id", x("topic"), x("message"), x("keywords"), x("optionType"), x("location"), x("effect"), x("time").toLong));
    }

    p.future
  }
  def store(not: Notification): Future[Notification] = {

    implicit val timeout = Timeout(1.seconds)

    import system.dispatcher

    var map = Map("topic" -> not.topic, "message" -> not.message, "keywords" -> not.keywords, "optionType" -> not.optionType,
      "location" -> not.location, "effect" -> not.effect, "time" -> not.time.toString())
    var ok: Future[String] = {
      val p = Promise[String]()
      redis ? Request("INCR", not.topic+"-not") onComplete {
        case Failure(e) => e.printStackTrace()
        case Success(Some(incr)) =>

          var id = not.topic + s":$incr"

          redis ? HashRequest("HMSET", id, map) onComplete {
            case Failure(e) =>
              println("error")
              e.printStackTrace()
            case Success(y) =>

              p.success(id.toString())

          }
      }

      p.future
    }

    var f: Future[Notification] = {
      val p = Promise[Notification]()
      ok onSuccess {
        case id =>
          println(id)
          var hash = for {
            Response.AsStringsHash(fields) ← redis ? Request("HGETALL", id)
          } yield fields

          hash onComplete {
            case Failure(e) =>
              println("error")
              e.printStackTrace()
            case Success(x: Map[String, String]) =>
              p.success(Notification(s"$id", x("topic"), x("message"), x("keywords"), x("optionType"), x("location"), x("effect"), x("time").toLong));
          }

      }
      p.future
    }
    f
  }
  def count(topic: String): Future[Long] = {
    implicit val timeout = Timeout(1.seconds)
    //vattopic= topic.trim()
    
    redis ? Request("GET", topic) map {
      case Some(count:ByteString) => 
        
      //  println(count.utf8String)
        count.utf8String.toLong
      case None => -1L  
    }
  }
  //def query(openedOn: Date): Future[Seq[Notification]] = ???
}

