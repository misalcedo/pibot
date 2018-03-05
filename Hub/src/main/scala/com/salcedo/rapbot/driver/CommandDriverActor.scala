package com.salcedo.rapbot.driver

import akka.actor.{Actor, ActorLogging, Props, Status}
import com.salcedo.rapbot.driver.CommandDriverActor.{Command, _}
import com.salcedo.rapbot.driver.DriverActor.{Drive, Replace, Update}
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot

object CommandDriverActor {

  sealed trait Command

  case object NoOp extends Command

  case object Brake extends Command

  case object Forward extends Command

  case object Reverse extends Command

  case object TurnLeft extends Command

  case object TurnRight extends Command

  case object FullThrottle extends Command

  case object Faster extends Command

  case object Slower extends Command

  def props: Props = Props(new CommandDriverActor(15, 5))
}

class CommandDriverActor(val throttleStep: Int, val orientationStep: Int) extends Actor with ActorLogging {
  override def receive: Receive = {
    case command: Command => this.drive(command)
  }

  def drive(command: Command): Unit = {
    val change = command match {
      case NoOp => Update()
      case Slower => Update(throttle = Some(-throttleStep))
      case Faster => Update(throttle = Some(throttleStep))
      case FullThrottle => Replace(throttle = Some(Int.MaxValue))
      case Brake => Replace(throttle = Some(0))
      case Forward => Replace(orientation = Some(90))
      case Reverse => Update(orientation = Some(-180))
      case TurnLeft => Update(orientation = Some(orientationStep))
      case TurnRight => Update(orientation = Some(-orientationStep))
    }

    sender() ! change
  }
}
