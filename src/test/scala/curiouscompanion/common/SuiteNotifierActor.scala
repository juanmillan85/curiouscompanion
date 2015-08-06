package curiouscompanion.common
import akka.testkit._
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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import curiouscompanion.model.Notification
/**
 * @author amb
 */
@RunWith(classOf[JUnitRunner])
class SuiteNotifierActor extends TestKit(ActorSystem("NotifierActorTest")) with FunSpecLike
    with BeforeAndAfter
    with ImplicitSender {
  import Connection._

  before {
    val brando = system.actorOf(Redis("localhost", 6379, 5, listeners = Set(self)))
    expectMsg(Connecting("localhost", 6379))
    expectMsg(Connected("localhost", 6379))
    brando ! Request("SELECT", "5")
    expectMsg(Some(Ok))
    //brando ! Request("FLUSHDB")
    //expectMsg(Some(Ok))
    brando ! Request("SET", "General-test", "0")
    expectMsg(Some(Ok))
    brando ! Request("SET", "General-not", "0")
    expectMsg(Some(Ok))
    brando ! Request("SET", "Financial-not", "0")
    expectMsg(Some(Ok))
    brando ! Request("SET", "Sports-not", "0")
    expectMsg(Some(Ok))
    brando ! Request("SET", "Fashion-not", "0")
    expectMsg(Some(Ok))

    var notifications = List(
      Notification("", "General", "More news on Greece crisis", "greece crisis athens", "error", "toast-bottom-left", "", 2000L),
      Notification("", "General", "Terror in Kenya", "kenya", "warning", "toast-top-right", "", 3000L),
      Notification("", "General", "London Remember", "london bombing attacks", "success", "toast-top-right", "", 2000L),
      Notification("", "General", "World news. Iran, China, Russia and more", "iran nuclear china russia", "error", "toast-bottom-right", "", 1000L),
      Notification("", "General", "Bill Cosby sexual assault", "Cosby sexual assault", "error", "toast-top-right", "", 3000L),
      Notification("", "General", "Mexico vs Donald trump", "mexico donald trump", "error", "toast-top-right", "", 1000L),
      Notification("", "General", "nice pics", "animal photo pet cat dog tree", "warning", "toast-top-right", "", 2000L),
      Notification("", "General", "Planing your vacations. Some news about tourism", "tourism beaches hotels airline", "info", "toast-top-right", "", 3000L),

      Notification("", "General", "Tech Stories", "Google Yahoo Facebook Microsoft Mozilla Twitter Oracle apps", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "travel, places and more", "travel places", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "Sciences & Research", "research science", "warning", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Ebola stories", "ebola", "error", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Lion stories", "lion cecil", "error", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Economic news", "debt revenue deficit tax income", "info", "toast-top-right", "", 1000L),
      Notification("", "General", "more on us president campaign", "clinton trump walker bush carson cruz huckabee rubio paul polls", "warning", "toast-top-right", "", 2000L),
      Notification("", "General", "General stock news", "price market stock", "info", "toast-top-right", "", 1000L),
      Notification("", "General", "Iran deal and negotiations", "iran", "warning", "toast-top-right", "", 3000L),
      Notification("", "General", "Economic news", "debt revenue deficit tax income", "info", "toast-top-right", "", 1000L),
      Notification("", "General", "Calais migrant crisis", "calais migrant", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "War news", "war", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "Best on social media", "best", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "Travel & Holidays", "relax holiday dreaming", "info", "toast-top-right", "", 2000L),
      //sports
      Notification("", "Sports", "Cricket news", "cricket Steven Finn", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Sports", "England football news", "chelsea manchester arsenal premier league manutd", "warning", "toast-top-right", "", 3000L),
      Notification("", "Sports", "Baseball", "ny baseball", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "Special Olympics", "specialolympics LA2015", "error", "toast-bottom-right", "", 3000L),
      Notification("", "Sports", "Basketball", "baseketball lakers knicks", "warning", "toast-top-right", "", 2000L),
      Notification("", "Sports", "Click to see news about Falcao", "falcao chelsea", "info", "toast-top-right", "", 1000L),
      Notification("", "Sports", "Champions pics", "champions winner gold", "warning", "toast-top-right", "", 3000L),
      Notification("", "Sports", "Tour the france", "nairo quintana froome france", "info", "toast-top-right", "", 1000L),
      Notification("", "Sports", "FIFA corruption", "corruption platini", "info", "toast-top-right", "", 2000L),
      Notification("", "Sports", "England sports", "uk", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Sports", "Kazan 2015", "kazan2015", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Sports", "Manchester news", "manutd Schweinsteiger Romero De Gea Rooney manchester", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "NFL and tom brady", "tom brady", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "Tom daley news", "tom daley", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "best on social media", "best", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "di maria transfer", "di maria psg", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "antidoping stories and news", "doping", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "rooney and everton", "rooney everton", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "Transfer & Gossip", "transfer gossip", "success", "toast-top-right", "", 1000L),
      Notification("", "Sports", "Iker Casillas", "iker casillas", "success", "toast-top-right", "", 1000L),
      Notification("", "Financial", "Click on  billionaires news", " billionaire success", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Financial", "Tips and tricks for success", "success tricks tips", "warning", "toast-top-right", "", 3000L),
      Notification("", "Financial", "More greece news", "greece euro merkel ", "success", "toast-top-right", "", 1000L),
      Notification("", "Financial", "UK and US inmigration", "inmigration ", "success", "toast-bottom-right", "", 3000L),
      Notification("", "Financial", "more on us president campaign", "clinton trump walker bush carson cruz huckabee rubio paul polls", "warning", "toast-top-right", "", 2000L),
      Notification("", "Financial", "General stock news", "price market stock", "info", "toast-top-right", "", 1000L),
      Notification("", "Financial", "Iran deal and negotiations", "iran", "warning", "toast-top-right", "", 3000L),
      Notification("", "Financial", "Economic news", "debt revenue deficit tax income", "info", "toast-top-right", "", 1000L),
      Notification("", "Financial", "Calais migrant crisis", "calais migrant", "info", "toast-top-right", "", 2000L),
      Notification("", "Financial", "England sports", "news uk", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Financial", "travel, places and more", "travel places", "info", "toast-top-right", "", 3000L),
      Notification("", "Financial", "Manchester news", "manutd Schweinsteiger Romero De Gea Rooney manchester", "success", "toast-top-right", "", 1000L),
      Notification("", "Financial", "Tom hayes jailed", "trader libor", "success", "toast-top-right", "", 1000L),
      
       Notification("", "Fashion", "Tom hayes jailed", "trader libor", "success", "toast-top-right", "", 1000L),
      Notification("", "General", "Stories on Terrorism", "Terrorism yemen tunisia ISIS attack", "error", "toast-top-right", "", 1000L))

    import curiouscompanion.repository._

    object repo extends AccountRepositoryRedis
    // repo.store(notifications(1))
    implicit val timeout = Timeout(1.seconds)
    import system.dispatcher
    notifications.foreach { not =>
      var f: Future[Notification] = repo.store(not)
      f onComplete {
        case Success(not) =>

          println(not.toString())
      }
    }

  }

  after {

  }

  describe("NotifierActor") {
    it("should get a total count notification test") {
      import curiouscompanion.repository._

      object repo extends AccountRepositoryRedis

      implicit val timeout = Timeout(1.seconds)
      import system.dispatcher
      repo.count("General") onSuccess {
        case res: Long => assert(res === 0L)
      }

    }

    it("add notification using repository") {

    }
    it("general options") {

      var subscribers = Set.empty[ActorRef]

      var typeMessages = List("error", "info", "success", "warning")
      var positions = List("toast-top-right", "toast-bottom-right", "toast-bottom-left")

      val brando = system.actorOf(Redis("localhost", 6379, 5, listeners = Set(self)))
      expectMsg(Connecting("localhost", 6379))
      expectMsg(Connected("localhost", 6379))
      brando ! Request("SELECT", "5")
      expectMsg(Some(Ok))

      brando ! Request("SET", "not-id", "0")
      expectMsg(Some(Ok))

      //brando ! Request("INCR", "count-not")
      //expectMsg(Some(11))

      brando ! Request("HGET", "mykey", "key")

      expectMsg(Some(ByteString("somevalue")))

      brando ! Request("HSET", "not", "messages", "Greek Crisis")

      expectMsg(Some(1))

      brando ! Request("HGET", "not", "messages")

      expectMsg(Some(ByteString("Greek Crisis")))

      var not = Notification("", "general", "Stories on Terrorism", "Terrorism yemen tunisia ISIS attack", "error", "toast-top-right", "", 1000L)
      var map = Map("topic" -> not.topic, "message" -> not.message, "keywords" -> not.keywords, "optionType" -> not.optionType,
        "location" -> not.location, "effect" -> not.effect, "time" -> not.time.toString())

      brando ! HashRequest("HMSET", "setkey", map)
      expectMsg(Some(Ok))

      implicit val timeout = Timeout(2.seconds)
      import system.dispatcher
      var key = for { Response.AsString(value) ← brando ? Request("HGET", "mykey", "key") } yield value
      key onComplete {
        case Success(s) => println("sss0" + s)
      }
      var hash = for {
        Response.AsStringsHash(fields) ← brando ? Request("HGETALL", "setkey")
      } yield fields
      hash.foreach(x => x.map(pair => pair._1 + "=" + pair._2).foreach { x => println(x) })
      hash onComplete {
        case Success(map) => map.foreach(println(_))
        case Failure(t)   => println("An error has occured: " + t.getMessage)
      }

    }

  }
}