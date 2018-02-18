package com.salcedo.rapbot.snapshot

import java.time.Instant
import java.util.UUID

import akka.actor.Status.Status
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status, Terminated}
import com.salcedo.rapbot.snapshot.SnapshotActor.{Snapshot, TakeSubSystemSnapshot}

import scala.concurrent.duration.Duration

object SnapshotActor {

  case class TakeSubSystemSnapshot(uuid: UUID)

  case class Snapshot(uuid: UUID, responses: Map[ActorRef, Status], duration: Duration)

  def props(uuid: UUID, subsystems: Set[ActorRef]): Props = Props(new SnapshotActor(uuid, subsystems))
}

class SnapshotActor(val uuid: UUID, subsystems: Set[ActorRef]) extends Actor with ActorLogging {
  var responses = Map.empty[ActorRef, Status]
  private val start = Instant.now

  override def preStart(): Unit = {
    subsystems.foreach(context.watch)
    subsystems.foreach(_ ! TakeSubSystemSnapshot(uuid))
  }

  override def receive: PartialFunction[Any, Unit] = {
    case failure: Status.Failure => this.fail(failure)
    case success: Status.Success => this.succeed(success)
    case terminated: Terminated => this.terminate(terminated)
  }

  private def fail(message: Status.Failure): Unit = {
    log.error("Received failure for {}.", sender, message.cause)
    addResponse(message)
  }

  private def succeed(message: Status.Success): Unit = {
    log.debug("Received success from {}. Message: {}", sender, message.status)
    addResponse(message)
  }

  private def terminate(terminated: Terminated): Unit = fail(Status.Failure(new IllegalStateException("Terminated")))

  private def addResponse(message: Status): Unit = {
    if (!subsystems.contains(sender)) return

    responses += (sender -> message)

    publish()
  }

  private def publish(): Unit = {
    if (!isComplete) return

    log.debug("Completed system snapshot '{}' with {} responses.", uuid, responses.size)

    val duration = java.time.Duration.between(start, Instant.now)

    context.system.eventStream.publish(Snapshot(uuid, responses, Duration.fromNanos(duration.toNanos)))
    context.stop(self)
  }

  private def isComplete: Boolean = responses.keySet.equals(subsystems)
}
