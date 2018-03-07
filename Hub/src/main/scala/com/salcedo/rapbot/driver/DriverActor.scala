package com.salcedo.rapbot.driver

import java.lang.Math._

import akka.actor.Status.Success
import akka.actor._
import com.salcedo.rapbot.driver.CommandDriverActor.Command
import com.salcedo.rapbot.driver.DriverActor.{Drive, DriveChange, Replace, Update}
import com.salcedo.rapbot.motor.MotorActor._
import com.salcedo.rapbot.motor.MotorActor.{Command, Location}
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.TakeSnapshot

object DriverActor {

  sealed trait DriveChange

  case class Update(orientation: Option[Int] = None, throttle: Option[Int] = None) extends DriveChange

  case class Replace(orientation: Option[Int] = None, throttle: Option[Int] = None) extends DriveChange

  case class Drive(orientation: Int, throttle: Int)

  def props(): Props = Props(new DriverActor(0 to 360, 0 to 255))
}


final class DriverActor(val orientationRange: Range, val throttleRange: Range) extends Actor with ActorLogging {
  val keyBoardDriver: ActorRef = context.actorOf(KeyBoardDriverActor.props(self))
  val commandDriver: ActorRef = context.actorOf(CommandDriverActor.props)
  var drive = Drive(orientation = 90, throttle = 0)

  override def receive: Receive = {
    case command: Command => commandDriver ! command
    case change: DriveChange => drive(change)
    case _: TakeSubSystemSnapshot => snapshot()
  }

  def snapshot(): Unit = {
    sender() ! Success(drive)
  }

  private def drive(change: DriveChange): Unit = {
    drive = change match {
      case Update(orientation, throttle) =>
        val updatedOrientation = drive.orientation + orientation.getOrElse(0)
        val updatedThrottle = drive.throttle + throttle.getOrElse(0)

        drive.copy(updatedOrientation, updatedThrottle)
      case Replace(orientation, throttle) =>
        drive.copy(orientation.getOrElse(drive.orientation), throttle.getOrElse(drive.throttle))
    }

    // TODO: bound throttle and make orientation circular
    drive = drive.copy(modulo(drive.orientation), bound(drive.throttle))

    context.system.eventStream.publish(vehicle(drive))
    context.system.eventStream.publish(TakeSnapshot)

    log.debug("Changed desired drive state to {}.", drive)
  }

  def bound(throttle: Int): Int = {
    max(throttleRange.head, min(throttleRange.last, throttle))
  }

  def modulo(orientation: Int): Int = {
    val distance = orientationRange.last - orientationRange.head
    val remainder = orientation % distance
    val negativeAdjustment = remainder + distance

    orientationRange.head + (negativeAdjustment % distance)
  }

  def vehicle(drive: Drive): Vehicle = {
    val orientation = toRadians(drive.orientation)
    val throttle = drive.throttle
    val adjustedThrottle = abs(floor(sin(orientation) * throttle)).toInt
    val command = if ((orientation - PI) > 0) Command.Backward else Command.Forward
    val leftSpeed = if (shouldAdjustRightMotor(orientation)) throttle else adjustedThrottle
    val rightSpeed = if (shouldAdjustRightMotor(orientation)) adjustedThrottle else throttle

    createMotorRequest(leftSpeed, rightSpeed, command)
  }

  private def shouldAdjustRightMotor(orientation: Double) = {
    val quadrant = orientation / (PI / 2)
    val inFirstQuadrant = quadrant >= 0.0 && quadrant < 1.0
    val inThirdQuadrant = quadrant > 2.0 && quadrant <= 3.0
    inFirstQuadrant || inThirdQuadrant
  }

  private def createMotorRequest(leftSpeed: Int, rightSpeed: Int, command: Command.Value): Vehicle = {
    Vehicle(
      Motor(leftSpeed, command, Location.BackLeft),
      Motor(rightSpeed, command, Location.BackRight)
    )
  }
}
