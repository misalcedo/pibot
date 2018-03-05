package com.salcedo.rapbot.hub

import java.nio.file.{Path, Paths}

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status, Terminated}
import com.salcedo.rapbot.driver.DriverActor.Drive
import com.salcedo.rapbot.hub.Hub.{NullSystem, SubSystem, System}
import com.salcedo.rapbot.motor.MotorActor.Vehicle
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import com.salcedo.rapbot.snapshot.SnapshotTakerActor
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.{RegisterSubSystem, TakeSnapshot}
import com.salcedo.rapbot.userinterface.{GraphicalUserInterface, GraphicalUserInterfaceActor}

object Hub {

  sealed trait System

  case class SubSystem(props: Props, name: String) extends System

  case class NullSystem() extends System

  case class SystemState(
                          snapshotId: String,
                          snapshotStartTimeInEpochMillis: Long,
                          snapshotEndTimeInEpochMillis: Option[Long],
                          driver: Int,
                          motor: Int,
                          image: String
                        )

  def props(gui: GraphicalUserInterface, driver: System, vision: System, motor: System): Props = {
    Props(new Hub(gui, driver, vision, motor))
  }
}

class Hub(gui: GraphicalUserInterface, driverSystem: System, visionSystem: System, motorSystem: System) extends Actor with ActorLogging {
  private val driver = actorFor(driverSystem)
  private val vision = actorFor(visionSystem)
  private val motor = actorFor(motorSystem)
  private val snapshot = context.actorOf(SnapshotTakerActor.props, "snapshot")
  private val ui = context.actorOf(GraphicalUserInterfaceActor.props(gui), "gui")

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Snapshot])

    snapshot ! RegisterSubSystem(driver)
    snapshot ! RegisterSubSystem(vision)
    snapshot ! RegisterSubSystem(motor)
    snapshot ! TakeSnapshot
  }

  override def receive: PartialFunction[Any, Unit] = {
    case terminated: Terminated => this.terminate(terminated)
    case snapshot: Snapshot => this.state(snapshot)
  }

  def actorFor(system: System): ActorRef = {
    system match {
      case SubSystem(props, name) => context.actorOf(props, name)
      case NullSystem() => context.actorOf(NullActor.props)
    }
  }

  private def state(snapshot: Snapshot): Unit = {
    val drive: Option[Drive] = snapshot.responses.get(driver)
      .filter(_.isInstanceOf[Status.Success])
      .map(_.asInstanceOf[Status.Success])
      .map(_.status)
      .filter(_.isInstanceOf[Drive])
      .map(_.asInstanceOf[Drive])

    val motorResponse: Option[Vehicle] = snapshot.responses.get(null)
      .filter(_.isInstanceOf[Status.Success])
      .map(_.asInstanceOf[Status.Success])
      .map(_.status)
      .filter(_.isInstanceOf[Vehicle])
      .map(_.asInstanceOf[Vehicle])

    val image: Option[Path] = snapshot.responses.get(vision)
      .filter(_.isInstanceOf[Status.Success])
      .map(_.asInstanceOf[Status.Success])
      .map(_.status)
      .filter(_.isInstanceOf[Path])
      .map(_.asInstanceOf[Path])

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
      motorResponse.map(_.backLeft)
        .map(_.toString)
        .getOrElse(""),
      motorResponse.map(_.backRight)
        .map(_.toString)
        .getOrElse(""),
      image.map(_.toAbsolutePath).getOrElse(Paths.get("/dev/null"))
    ))
  }

  private def terminate(message: Terminated): Unit = {
    log.error("Actor terminated: {}. Shutting down system.", message.actor)
  }
}

