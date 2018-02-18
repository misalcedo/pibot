package com.salcedo.rapbot.hub

import java.nio.file.{Path, Paths}

import akka.actor.{Actor, ActorLogging, Props, Status, Terminated}
import com.salcedo.rapbot.driver.{DriveState, DriverActor}
import com.salcedo.rapbot.locomotion.{Location, MotorActor, MotorResponse}
import com.salcedo.rapbot.sense.SenseActor
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import com.salcedo.rapbot.snapshot.SnapshotTakerActor
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.{RegisterSubSystem, TakeSnapshot}
import com.salcedo.rapbot.userinterface.{GraphicalUserInterface, GraphicalUserInterfaceActor}
import com.salcedo.rapbot.vision.VisionActor

object Hub {

  case class SystemState(
                          snapshotId: String,
                          snapshotStartTimeInEpochMillis: Long,
                          snapshotEndTimeInEpochMillis: Option[Long],
                          driver: Int,
                          motor: Int,
                          image: String
                        )

  def props(serviceFactory: ServiceFactory, gui: GraphicalUserInterface, workingDirectory: Path): Props = {
    Props(new Hub(serviceFactory, gui, workingDirectory))
  }
}

class Hub(serviceFactory: ServiceFactory, gui: GraphicalUserInterface, workingDirectory: Path) extends Actor with ActorLogging {
  private val motors = context.actorOf(MotorActor.props(serviceFactory.motor), "motor")
  private val vision = context.actorOf(VisionActor.props(serviceFactory.vision), "vision")
  private val sensors = context.actorOf(SenseActor.props(serviceFactory.sense), "sense")
  private val driver = context.actorOf(DriverActor.props(motors), "driver")
  private val snapshot = context.actorOf(SnapshotTakerActor.props, "snapshot")
  private val ui = context.actorOf(GraphicalUserInterfaceActor.props(gui), "gui")

  override def preStart(): Unit = {
    log.info("Starting Hub with working directory of: {}.", workingDirectory)

    context.system.eventStream.subscribe(self, classOf[Snapshot])

    snapshot ! RegisterSubSystem(vision)
    snapshot ! RegisterSubSystem(driver)
    snapshot ! RegisterSubSystem(motors)
    snapshot ! RegisterSubSystem(sensors)
    snapshot ! TakeSnapshot()
  }

  override def receive: PartialFunction[Any, Unit] = {
    case terminated: Terminated => this.terminate(terminated)
    case snapshot: Snapshot => this.state(snapshot)
  }


  private def state(snapshot: Snapshot): Unit = {
    val driveState: Option[DriveState] = snapshot.responses.get(driver)
      .filter(_.isInstanceOf[Status.Success])
      .map(_.asInstanceOf[Status.Success])
      .map(_.status)
      .filter(_.isInstanceOf[DriveState])
      .map(_.asInstanceOf[DriveState])

    val motorResponse: Option[MotorResponse] = snapshot.responses.get(motors)
      .filter(_.isInstanceOf[Status.Success])
      .map(_.asInstanceOf[Status.Success])
      .map(_.status)
      .filter(_.isInstanceOf[MotorResponse])
      .map(_.asInstanceOf[MotorResponse])

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
        .map(_.toStringWithoutAddress),
      snapshot.duration,
      driveState.map(_.getThrottle).getOrElse(0),
      driveState.map(_.getOrientation).getOrElse(0),
      motorResponse.map(_.getMotor(Location.BACK_LEFT))
        .map(_.toString)
        .getOrElse(""),
      motorResponse.map(_.getMotor(Location.BACK_RIGHT))
        .map(_.toString)
        .getOrElse(""),
      image.map(_.toAbsolutePath).getOrElse(Paths.get("/dev/null"))
    ))
    // TODO: publish SystemState case class, Hub is the only class that should know the names of its child actors.
  }

  private def terminate(message: Terminated): Unit = {
    log.error("Actor terminated: {}. Shutting down system.", message.actor)
  }
}

