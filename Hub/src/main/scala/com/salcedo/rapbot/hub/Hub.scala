package com.salcedo.rapbot.hub

import java.time.Instant

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.salcedo.rapbot.driver.DriverActor.Drive
import com.salcedo.rapbot.hub.Hub.{SubSystem, SystemState}
import com.salcedo.rapbot.motor.MotorActor.Vehicle
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import com.salcedo.rapbot.snapshot.SnapshotTakerActor
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.{RegisterSubSystem, TakeSnapshot}
import com.salcedo.rapbot.vision.VisionActor.StillFrame

object Hub {

  def props(subSystems: SubSystem*): Props = {
    Props(new Hub(subSystems))
  }

  case class SubSystem(props: Props, name: String)

  case class SystemState(
                          trigger: String,
                          uuid: String,
                          start: Long,
                          timeWindow: Long,
                          drive: Drive,
                          vehicle: Vehicle,
                          imagePath: String
                        )

}

class Hub(subSystems: Seq[SubSystem]) extends Actor with ActorLogging {
  private val snapshot = context.actorOf(SnapshotTakerActor.props, "snapshot")
  var children: Set[ActorRef] = subSystems.map(actorFor).toSet

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Snapshot])

    children.foreach(snapshot ! RegisterSubSystem(_))
    snapshot ! TakeSnapshot(self.path.name)
  }

  override def receive: PartialFunction[Any, Unit] = {
    case terminated: Terminated => this.terminate(terminated)
    case snapshot: Snapshot => this.state(snapshot)
  }

  private def state(snapshot: Snapshot): Unit = {
    val snapshots = snapshot.responses.values
      .filter(_.isInstanceOf[Success])
      .map(_.asInstanceOf[Success])
      .map(_.status)

    val drive = snapshots.find(_.isInstanceOf[Drive]).map(_.asInstanceOf[Drive])
    val vehicle: Option[Vehicle] = snapshots.find(_.isInstanceOf[Vehicle]).map(_.asInstanceOf[Vehicle])
    val image: Option[StillFrame] = snapshots.find(_.isInstanceOf[StillFrame]).map(_.asInstanceOf[StillFrame])

    context.system.eventStream.publish(SystemState(
      snapshot.trigger,
      snapshot.uuid.toString,
      snapshot.start.toEpochMilli,
      snapshot.duration.toMillis,
      drive.orNull,
      vehicle.orNull,
      image.map(_.path).getOrElse("/dev/null")
    ))
  }

  private def terminate(message: Terminated): Unit = {
    log.error("Actor terminated: {}.", message.actor)

    children -= message.actor
  }

  def actorFor(system: SubSystem): ActorRef = {
    context.watch(context.actorOf(system.props, system.name))
  }
}

