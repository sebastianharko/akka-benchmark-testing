package testing

import akka.actor.{ActorSystem, Props, ReceiveTimeout}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.persistence.{PersistentActor, Recovery}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.json4s.{DefaultFormats, jackson}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system = ActorSystem("system")

  val pongers = ClusterSharding(system).start(
    typeName = "pongers",
    entityProps = Ponger.props,
    settings = ClusterShardingSettings(system),
    extractShardId = Ponger.extractShardId,
    extractEntityId = Ponger.extractEntityId
  )

  import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

  // for JSON serialization / deserialization
  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  implicit val timeout = Timeout(3 seconds)

  implicit val dispatcher = system.dispatcher

  implicit val materialization = ActorMaterializer()

  val route =
    path("ping") {
      post {
        entity(as[Ping]) {
          ping => {
            onComplete((pongers ? ping).mapTo[Pong]) {
              case Success(v) => complete(OK -> v)
              case Failure(_) => complete(InternalServerError -> Map("message" -> "internal server error"))
            }
          }
        }
      }
    }

  Http().bindAndHandle(route, "localhost", 8080)

}


sealed trait Event
sealed trait Command
sealed trait Response

case class Ping(id: String) extends Command
case class Pinged(time: Long) extends Event
case class Pong(id: String, time: Long) extends Response

case object Stop

object Ponger {


  def props = Props(new Ponger)


  def extractShardId: ShardRegion.ExtractShardId = {
    case msg @ Ping(id) => id.charAt(0).toString
  }

  def extractEntityId: ShardRegion.ExtractEntityId = {
    case msg @ Ping(id) => (msg.id, msg)
  }

}

class Ponger extends PersistentActor {

  override def recovery = Recovery.none

  var numReceivedPings = 0

  import ShardRegion.Passivate

  context.setReceiveTimeout(30.seconds)

  override def receiveRecover = {
    case Pinged(_) => numReceivedPings += 1
  }

  override def receiveCommand = {
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = Stop)
    case Stop => context.stop(self)
    case Ping(id) =>
      val timeStamp = System.currentTimeMillis()
      persist(Pinged(timeStamp)) {
        event => {
          numReceivedPings += 1
          sender() ! Pong(id, timeStamp)
        }
      }
  }

  override def persistenceId: String = self.path.name
}



