package com.salcedo.rapbot.vision

import java.io.{IOException, UncheckedIOException}
import java.nio.file.Files.{createDirectories, createTempFile}
import java.nio.file.{Path, Paths}

import akka.actor.{Actor, ActorLogging, Props}
import akka.dispatch.Futures
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, FlowShape, IOResult}
import com.salcedo.rapbot.snapshot.RemoteSnapshot
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot
import com.salcedo.rapbot.vision.VisionActor.See

import scala.concurrent.{Future, Promise}
import akka.pattern.pipe
import akka.stream.scaladsl.FileIO

object VisionActor {

  case class See()

  def props(uri: Uri, workingDirectory: Path): Props = Props(new VisionActor(uri, workingDirectory))
}

final class VisionActor(val uri: Uri, val workingDirectory: Path) extends Actor with RemoteSnapshot with ActorLogging {
  import context.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  override def receive: Receive = {
    case _: See => this.see()
    case _: TakeSubSystemSnapshot => this.snapshot()
  }

  def see(): Unit = {
    pipe(remoteSnapshot).to(sender(), self)
  }

  override def remoteSnapshot: Future[Path] = {
    http.singleRequest(HttpRequest(uri = uri.withPath(Uri.Path("/still.jpg"))))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          val path = temporaryPath()
          val promise: Promise[Path] = Futures.promise()

          entity.dataBytes
            .runWith[Future[IOResult]](FileIO.toPath(path))
            .map(_.status).map(_.map(_ => path)).map(promise.complete)

          promise.future
        case HttpResponse(statusCode, _, entity, _) =>
          entity.discardBytes()
          sys.error(s"Received a response with a status code other than 200 OK. Status: $statusCode")
      }
  }

  private def temporaryPath(): Path = {
    try {
      createTempFile(createDirectories(workingDirectory.resolve("images")), "image", ".jpg")
    } catch {
      case e: IOException => throw new UncheckedIOException(e)
    }
  }
}
