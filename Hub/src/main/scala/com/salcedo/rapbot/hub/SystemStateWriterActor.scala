package com.salcedo.rapbot.hub

import java.io._
import java.nio.file.Files.{createDirectories, createTempFile}
import java.nio.file.Path

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, Props}
import com.salcedo.rapbot.hub.Hub.SystemState
import com.salcedo.rapbot.serialization.{JSON, Serializer}
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot

object SystemStateWriterActor {
  def props(workingDirectory: Path): Props = {
    Props(new SystemStateWriterActor(workingDirectory, JSON, 100))
  }
}

class SystemStateWriterActor(workingDirectory: Path, serializer: Serializer, entriesPerFile: Int) extends Actor with ActorLogging {
  var currentFile: Option[Path] = None
  var currentWriter: PrintWriter = new PrintWriter("/dev/null")
  var entriesInFile = 0

  override def preStart(): Unit = {
    rotateFile()
    context.system.eventStream.subscribe(self, classOf[SystemState])
  }

  override def receive: Receive = {
    case state: SystemState => writeToFile(state)
    case _: TakeSubSystemSnapshot => sender() ! Success(None)
  }

  def writeToFile(state: SystemState): Unit = {
    rotateFile()

    try {
      currentWriter.println(serializer.write(state))
    } catch {
      case e: IOException => throw new UncheckedIOException(e)
    } finally {
      currentWriter.close()
    }
  }

  private def rotateFile(): Unit = {
    if (entriesPerFile > entriesInFile) {
      return
    }

    try {
      val path = workingDirectory.resolve("state").resolve("snapshots.json")
      val file = createTempFile(createDirectories(path), "snapshot", ".json").toAbsolutePath
      val fileWriter = new FileWriter(file.toFile, true)
      val bufferedWriter = new BufferedWriter(fileWriter)

      log.info("Rotating file with {}/{} entries from {} to {}.", entriesInFile, entriesPerFile, file, currentFile.orNull)

      currentFile = Some(file)
      currentWriter = new PrintWriter(bufferedWriter)
    } catch {
      case e: IOException => throw new UncheckedIOException(e)
    }
  }
}
