package com.salcedo.rapbot.motor

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{JsonFraming, Sink}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.salcedo.rapbot.motor.MotorActor.Vehicle
import com.salcedo.rapbot.snapshot.RemoteSnapshot
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.ext.EnumSerializer

import scala.concurrent.Future

object Location extends Enumeration {
  val BackLeft, BackRight = Value
}

object Command extends Enumeration {
  val Forward, Backward, Brake, Release = Value
}

object MotorActor {

  final case class Vehicle(backLeft: Motor, backRight: Motor)

  final case class Motor(speed: Int, command: Command.Value, location: Location.Value)

  def props(uri: Uri): Props = Props(new MotorActor(uri))
}

class MotorActor(val uri: Uri) extends Actor with RemoteSnapshot with ActorLogging {

  import context.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  implicit val formats: Formats = DefaultFormats + new EnumSerializer(Location) + new EnumSerializer(Command)

  val http = Http(context.system)

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Vehicle])
  }

  override def receive: PartialFunction[Any, Unit] = {
    case vehicle: Vehicle => this.update(vehicle)
    case _: TakeSubSystemSnapshot => this.snapshot()
  }

  def update(vehicle: Vehicle): Unit = {
    val request = HttpRequest(
      uri = uri.withPath(Uri.Path("/motors")),
      method = HttpMethods.PUT,
      entity = HttpEntity(ContentTypes.`application/json`, write(vehicle))
    )

    http.singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.map(_.utf8String).runWith(Sink.seq).onComplete(f => log.info("Updated motors: {}", f.get.head))

        entity.discardBytes()
        Future()
      case HttpResponse(statusCode, _, entity, _) =>
        entity.discardBytes()
        sys.error(s"Received a response with a status code other than 200 OK. Status: $statusCode")
    }
  }

  override def remoteSnapshot: Future[Vehicle] = {
    http.singleRequest(HttpRequest(uri = uri.withPath(Uri.Path("/motors")))).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        parseVehicle(entity)
      case HttpResponse(statusCode, _, entity, _) =>
        entity.discardBytes()
        sys.error(s"Received a response with a status code other than 200 OK. Status: $statusCode")
    }
  }

  private def parseVehicle(entity: ResponseEntity): Future[Vehicle] = {
    entity.transformDataBytes(JsonFraming.objectScanner(256))
      .dataBytes
      .map(_.utf8String)
      .map(read[Vehicle])
      .runWith(Sink.seq)
      .map(_.head)
  }
}
