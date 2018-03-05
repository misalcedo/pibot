package com.salcedo.rapbot.vision

import java.io.{IOException, UncheckedIOException}
import java.nio.file.Files.{createDirectories, createTempFile, deleteIfExists}
import java.nio.file.{Path, Paths}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.pipe
import com.salcedo.rapbot.snapshot.RemoteSnapshot
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot
import com.salcedo.rapbot.vision.LocalVisionActor.SnapshotTaken
import com.salcedo.rapbot.vision.VisionActor.See
import uk.co.caprica.vlcj.player.{MediaPlayer, MediaPlayerEventAdapter}

import scala.concurrent.{Future, Promise}

object LocalVisionActor {
  case class SnapshotTaken(filename: String)
  def props(mediaPlayer: MediaPlayer, workingDirectory: Path): Props = Props(new LocalVisionActor(mediaPlayer, workingDirectory))
}

class SnapshotListener(val actor: ActorRef) extends MediaPlayerEventAdapter {
  override def snapshotTaken(mediaPlayer: MediaPlayer, filename: String): Unit = {
    actor ! SnapshotTaken(filename)
  }
}

class LocalVisionActor(val mediaPlayer: MediaPlayer, val workingDirectory: Path) extends Actor with RemoteSnapshot with ActorLogging {
  import context.dispatcher

  val listener = new SnapshotListener(self)
  var requests: Map[String, Promise[Path]] = Map()

  override def preStart(): Unit = {
    requests = requests.empty

    mediaPlayer.removeMediaPlayerEventListener(listener)
    mediaPlayer.addMediaPlayerEventListener(listener)
  }

  override def receive: PartialFunction[Any, Unit] = {
    case _: See => this.see()
    case snapshotTaken: SnapshotTaken => this.succeed(snapshotTaken)
    case _: TakeSubSystemSnapshot => this.snapshot()
  }

  def see(): Unit = {
    pipe(remoteSnapshot).to(sender(), self)
  }

  def succeed(snapshotTaken: SnapshotTaken): Unit = {
    requests(snapshotTaken.filename).success(Paths.get(snapshotTaken.filename))
    requests -= snapshotTaken.filename
  }

  override def remoteSnapshot: Future[Path] = {
    val promise: Promise[Path] = Promise()

    try {
      val path = temporaryPath()
      val success = mediaPlayer.saveSnapshot(path.toAbsolutePath.toFile)

      if (success)
        requests += (path.toAbsolutePath.toString -> promise)
      else {
        promise.failure(new IllegalStateException("Failed to save snapshot."))
        deleteIfExists(path)
      }
    } catch {
      case e: IOException => promise.failure(e)
    }

    promise.future
  }

  private def temporaryPath(): Path = {
    try {
      createTempFile(createDirectories(workingDirectory.resolve("images")), "image", ".png")
    } catch {
      case e: IOException => throw new UncheckedIOException(e)
    }
  }
}
