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
      Notification("", "General", "Looking around the !", "world", "success", "toast-top-right", "", 2000L),
      Notification("", "General", "Refugees", "Syria", "error", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Full of sound and fury..", "Middle East", "error", "toast-bottom-left", "", 2000L),
      Notification("", "General", "Who visited Kenya?", "kenya", "warning", "toast-top-right", "", 3000L),
      Notification("", "General", "Remember: 70 years ago?", "boming hiroshima", "success", "toast-top-right", "", 2000L),
      Notification("", "General", "Follow world news. Iran, China, Russia and more", "iran nuclear china russia", "error", "toast-bottom-right", "", 5000L),
      Notification("", "General", "Sexual scandal. 35 women have accused him!!", "Cosby sexual assault", "error", "toast-top-right", "", 3000L),
      Notification("", "General", "Find news about the Iraq inquiry", "chilcot", "error", "toast-top-right", "", 5000L),
      Notification("", "General", "Mexico vs Donald trump", "mexico donald trump", "warning", "toast-bottom-right", "", 5000L),
      Notification("", "General", "nice pics", "animal photo pet underwater", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "When will this end?", "seaworld whales blackfish orcas", "warning", "toast-bottom-left", "", 3000L),
      Notification("", "General", "Planing your vacations. Some news about tourism", "tourism beaches hotels airline", "info", "toast-top-right", "", 3000L),
      Notification("", "General", "Devastation in Orient", "china explosions tianjin", "info", "toast-top-right", "", 3000L),
      Notification("", "General", "Tech Stories", "Google Yahoo Facebook Microsoft Mozilla Twitter Oracle apps", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "travel, places and more", "travel places", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "Sciences & Research", "research science geo", "warning", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Breast reduction?", "modern family", "warning", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Two things are infinite: human stupidity and...", "galaxies earth nasa satellite asteroid universe", "error", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Ebola stories", "ebola", "error", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Sexy, funny, accessible, enigmatic. Who was she?", "Marilyn Monroe", "warning", "toast-bottom-left", "", 2000L),
      Notification("", "General", "The only escape from the miseries of life are music and ....", "cats kitten", "warning", "toast-bottom-left", "", 4000L),

      Notification("", "General", "Lion stories. Who killed Cecil?", "lion cecil", "error", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Economic news", "debt revenue deficit tax income", "info", "toast-top-right", "", 3000L),
      Notification("", "General", "more on us president campaign", "clinton trump walker bush carson cruz huckabee rubio paul polls", "warning", "toast-bottom-right", "", 2000L),
      Notification("", "General", "General stock news", "price market stock", "info", "toast-top-right", "", 5000L),
      Notification("", "General", "Nuclear alert", "nuclear explosion", "info", "toast-bottom-right", "", 3000L),
      Notification("", "General", "Iran deal and negotiations", "iran", "warning", "toast-top-right", "", 3000L),
      Notification("", "General", "Economic news", "debt tax income", "info", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Calais migrant crisis", "calais migrant", "info", "toast-top-right", "", 4000L),
      Notification("", "General", "War news", "war", "info", "toast-top-right", "", 4000L),
      Notification("", "General", "Best on social media", "best", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "Travel & Holidays", "relax holiday dreaming", "info", "toast-bottom-right", "", 3000L),
      Notification("", "General", "Labour 'annihilation'?", "labour jeremy corbyn", "info", "toast-top-right", "", 2000L),
      Notification("", "General", "Shoreham. What happened'?", "Shoreham air disaster", "warning", "toast-bottom-right", "", 4000L),
      Notification("", "General", "News about london's most important public transport", "tube strike", "success", "toast-bottom-left", "", 2000L),
      Notification("", "General", "20 years ago..", "windows 95 microsoft", "error", "toast-bottom-left", "", 3000L),
      Notification("", "General", "is this the end?", "one direction", "warning", "toast-bottom-right", "", 3000L),
      Notification("", "General", "A dark day ...", "blackmonday", "error", "toast-bottom-left", "", 4000L),
      Notification("", "General", "Hope for mankind", "vaccine flu", "success", "toast-top-right", "", 2000L),
      Notification("", "General", "A big ice chunk...", "greenland", "warning", "toast-bottom-right", "", 2000L),
      Notification("", "General", "Find more news about World Athletics Championships", "World Championships Beijing2015", "success", "toast-bottom-left", "", 2000L),
      Notification("", "General", "Stories on Terrorism", "Terrorism yemen tunisia ISIS attack", "error", "toast-top-right", "", 5000L),

      //sports
      Notification("", "Sports", "Find more news about Cricket", "cricket Steven Finn", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Sports", "England football news", "chelsea manchester arsenal premier league manutd", "warning", "toast-top-right", "", 3000L),
      Notification("", "Sports", "Baseball", "yankees baseball", "success", "toast-top-right", "", 5000L),
      Notification("", "Sports", "Basketball", "baseketball lakers knicks", "warning", "toast-top-right", "", 2000L),
      Notification("", "Sports", "Click to see news about Falcao", "falcao chelsea", "info", "toast-top-right", "", 5000L),
      Notification("", "Sports", "Champions pics", "champions winner gold", "warning", "toast-bottom-left", "", 3000L),
      Notification("", "Sports", "Tour the france", "france tour quintana froome", "info", "toast-top-right", "", 5000L),
      Notification("", "Sports", "FIFA corruption", "corruption platini", "info", "toast-bottom-right", "", 2000L),
      Notification("", "Sports", "Sports", "sports win", "error", "toast-bottom-left", "", 2000L),
      Notification("", "Sports", "Looking around the !", "world", "success", "toast-top-right", "", 2000L),
     // Notification("", "Sports", "Kazan 2015", "kazan2015", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Sports", "Manchester news", "manutd Schweinsteiger Rooney manchester", "success", "toast-top-right", "", 5000L),
      Notification("", "Sports", "Find more news about World Athletics Championships", "World Championships Beijing2015", "success", "toast-bottom-left", "", 2000L),
      Notification("", "Sports", "NFL and tom brady", "tom brady nfl", "success", "toast-top-right", "", 5000L),
      Notification("", "Sports", "Tom daley news", "tom daley", "success", "toast-bottom-right", "", 3000L),
      Notification("", "Sports", "best on social media", "best", "success", "toast-bottom-left", "", 5000L),
      Notification("", "Sports", "Welcome to france..transfer", "di maria psg", "success", "toast-bottom-right", "", 5000L),
      Notification("", "Sports", "Antidoping stories and news", "doping", "error", "toast-top-right", "", 2000L),
      Notification("", "Sports", "rooney and everton", "rooney everton", "info", "toast-bottom-left", "", 5000L),
      Notification("", "Sports", "Back to Italy", "cuadrado", "success", "toast-top-right", "", 2000L),
      Notification("", "Sports", "Transfer & Gossip", "transfer gossip", "error", "toast-top-right", "", 3000L),
      Notification("", "Sports", "Iker Casillas", "iker casillas", "success", "toast-top-right", "", 5000L),
      Notification("", "Sports", "Blues' transfers from Spain", "pedro chelsea", "info", "toast-bottom-right", "", 5000L),
      Notification("", "Sports", "Badboy returns to Millan", "balotelli millan", "error", "toast-bottom-left", "", 5000L),
      Notification("", "Sports", "Cincinnati Masters", "Cincinnati Masters", "warning", "toast-bottom-left", "", 5000L),
      Notification("", "Sports", "Arsenal transfers", "Edinson Cavani Cech", "error", "toast-bottom-left", "", 5000L),
      Notification("", "Sports", "The Scorpion", "Higuita", "success", "toast-bottom-right", "", 5000L),
      Notification("", "Sports", "Tragedy", "indycar", "error", "toast-bottom-left", "", 5000L),
      Notification("", "Sports", "Tennis news", "usopen tennis", "success", "toast-bottom-left", "", 5000L),
      //finance
      Notification("", "Financial", "Looking around the !", "world", "success", "toast-top-right", "", 2000L),
      Notification("", "Financial", "Refugees", "Syria", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Financial", "Gossip, Stories and titles", "CEO CTO CIO", "info", "toast-top-right", "", 2000L),
      Notification("", "Financial", "Click on  billionaires news", "billionaire success", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Financial", "Tips and tricks for success", "success tricks tips", "warning", "toast-top-right", "", 3000L),
      Notification("", "Financial", "More greece news", "greece euro merkel", "info", "toast-top-right", "", 5000L),
      Notification("", "Financial", "UK and US inmigration", "inmigration", "success", "toast-bottom-right", "", 3000L),
      Notification("", "Financial", "Find more on us president campaign", "clinton trump walker bush carson cruz huckabee rubio paul polls", "warning", "toast-top-right", "", 2000L),
      Notification("", "Financial", "General stock news", "price market stock", "error", "toast-bottom-left", "", 5000L),
      Notification("", "Financial", "Click to follow Cashtags", "$AMZN $MSFT", "success", "toast-bottom-right", "", 3000L),
      Notification("", "Financial", "Iran deal and negotiations", "iran", "warning", "toast-top-right", "", 3000L),
      Notification("", "Financial", "Economic news", "debt revenue deficit tax income", "success", "toast-bottom-right", "", 5000L),
      Notification("", "Financial", "Calais migrant crisis", "calais migrant", "info", "toast-bottom-left", "", 2000L),
      Notification("", "Financial", "Bombs, War and Terrorism", "war hate terrorism", "error", "toast-bottom-right", "", 2000L),
      Notification("", "Financial", "Discover, travel, and more", "travel places", "info", "toast-top-right", "", 3000L),
      Notification("", "Financial", "north darkness!!", "north korea", "warning", "toast-bottom-left", "", 3000L),
      Notification("", "Financial", "A dark day ...", "blackmonday", "error", "toast-bottom-right", "", 4000L),
      Notification("", "Financial", "Tom hayes jailed", "trader libor", "success", "toast-top-right", "", 2000L),
      Notification("", "Financial", "Cashtags", "$SPY $YAHOO", "success", "toast-bottom-right", "", 3000L),
      Notification("", "Financial", "Asia minor in war", "turkey kurds", "info", "toast-bottom-right", "", 4000L),
      Notification("", "Financial", "un-happiest place on earth", "Banksy Dismaland", "error", "toast-top-right", "", 5000L),
      Notification("", "Financial", "China depression?", "china market stocks", "error", "toast-bottom-left", "", 4000L),
      Notification("", "Financial", "What is happenning to oil prices?", "oil", "warning", "toast-bottom-right", "", 2000L),
      Notification("", "Financial", "Cashtags", "$AAPL $GOOG $FB", "warning", "toast-bottom-left", "", 3000L),
      Notification("", "Financial", "Baharat", "india", "error", "toast-top-right", "", 2000L),
      Notification("", "Financial", "Gossip, Stories and titles", "CEO CTO CIO", "info", "toast-top-right", "", 2000L),
      Notification("", "Financial", "Should you be worry?", "El Niño", "info", "toast-top-right", "", 2000L),

      Notification("", "Financial", "A state of reduced spending and increased frugality in the financial sector", "Austerity", "success", "toast-top-right", "", 2000L),

      // Notification("", "Fashion", "Tom hayes jailed", "trader libor", "success", "toast-top-right", "", 5000L),
      Notification("", "Financial", "Stories on Terrorism", "Terrorism ISIS", "error", "toast-top-right", "", 5000L))

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

      /*      var subscribers = Set.empty[ActorRef]

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

      var not = Notification("", "general", "Stories on Terrorism", "Terrorism yemen tunisia ISIS attack", "error", "toast-top-right", "", 5000L)
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
*/
    }

  }
}
