package com.salcedo.rapbot.hub

import java.nio.file.{Path, Paths}

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status, Terminated}
import com.salcedo.rapbot.driver.DriverActor.Drive
import com.salcedo.rapbot.hub.Hub.SubSystem
import com.salcedo.rapbot.motor.MotorActor.Vehicle
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import com.salcedo.rapbot.snapshot.SnapshotTakerActor
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.{RegisterSubSystem, TakeSnapshot}
import com.salcedo.rapbot.userinterface.{GraphicalUserInterface, GraphicalUserInterfaceActor}

object Hub {

  case class SubSystem(props: Props, name: String)

  case class SystemState(
                          uuid: String,
                          timeWindow: Long,
                          drive: Drive,
                          vehicle: Vehicle,
                          imagePath: String
                        )

  def props(gui: GraphicalUserInterface, subSystems: SubSystem*): Props = {
    Props(new Hub(gui, subSystems))
  }
}

class Hub(gui: GraphicalUserInterface, subSystems: Seq[SubSystem]) extends Actor with ActorLogging {
  private val children = subSystems.map(actorFor)
  private val snapshot = context.actorOf(SnapshotTakerActor.props, "snapshot")
  private val ui = context.actorOf(GraphicalUserInterfaceActor.props(gui), "gui")

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Snapshot])

    children.foreach(snapshot ! RegisterSubSystem(_))
    snapshot ! TakeSnapshot
  }

  override def receive: PartialFunction[Any, Unit] = {
    case terminated: Terminated => this.terminate(terminated)
    case snapshot: Snapshot => this.state(snapshot)
  }

  def actorFor(system: SubSystem): ActorRef = {
    context.watch(context.actorOf(system.props, system.name))
  }

  private def state(snapshot: Snapshot): Unit = {
    val snapshots = snapshot.responses.values
      .filter(_.isInstanceOf[Success])
      .map(_.asInstanceOf[Success])
      .map(_.status)

    val drive = snapshots.find(_.isInstanceOf[Drive]).map(_.asInstanceOf[Drive])
    val vehicle: Option[Vehicle] = snapshots.find(_.isInstanceOf[Vehicle]).map(_.asInstanceOf[Vehicle])
    val image: Option[Path] = snapshots.find(_.isInstanceOf[Path]).map(_.asInstanceOf[Path])

    context.system.eventStream.publish(new SnapshotBackedSystemState(
      snapshot.uuid,
      snapshot.responses
        .filter(_._2.isInstanceOf[Status.Success])
        .keySet
        .map(_.path)
        .map(_.name),
      snapshot.duration,
      drive.map(_.throttle).getOrElse(0),
      drive.map(_.orientation).getOrElse(90),
      vehicle.map(_.backLeft)
        .map(_.toString)
        .getOrElse(""),
      vehicle.map(_.backRight)
        .map(_.toString)
        .getOrElse(""),
      image.map(_.toAbsolutePath).getOrElse(Paths.get("/dev/null"))
    ))

    context.system.eventStream.publish(Hub.SystemState(
      snapshot.uuid.toString,
      snapshot.duration.toMillis,
      drive.orNull,
      vehicle.orNull,
      image.map(_.toAbsolutePath.toString).getOrElse("/dev/null")
    ))
  }

  private def terminate(message: Terminated): Unit = {
    log.error("Actor terminated: {}.", message.actor)
  }
}

