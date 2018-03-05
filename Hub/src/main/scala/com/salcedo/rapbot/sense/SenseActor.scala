package com.salcedo.rapbot.sense

import akka.actor.{Actor, ActorLogging, Props}
import akka.dispatch.Futures
import com.salcedo.rapbot.sense.SenseActor._
import com.salcedo.rapbot.snapshot.RemoteSnapshot
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot

import scala.concurrent.Future

object SenseActor {

  case class OrientationRequest()

  case class Orientation(yaw: Double, pitch: Double, roll: Double)

  case class AccelerationRequest()

  case class Acceleration(x: Double, y: Double, z: Double)

  case class GyroscopeRequest()

  case class Gyroscope(x: Double, y: Double, z: Double)

  case class CompassRequest()

  case class Compass(north: Double)

  case class TemperatureRequest()

  case class Temperature(celsius: Double)

  case class HumidityRequest()

  case class Humidity(atmospheres: Double)

  case class Environment(
                          orientation: Orientation,
                          acceleration: Acceleration,
                          gyroscope: Gyroscope,
                          compass: Compass,
                          temperature: Temperature,
                          humidity: Humidity
                        )

  def props(): Props = Props(new SenseActor())
}

class SenseActor() extends Actor with RemoteSnapshot with ActorLogging {
  override def receive: PartialFunction[Any, Unit] = {
    case _: OrientationRequest => this.orientation()
    case _: AccelerationRequest => this.acceleration()
    case _: GyroscopeRequest => this.gyroscope()
    case _: CompassRequest => this.compass()
    case _: TemperatureRequest => this.temperature()
    case _: HumidityRequest => this.humidity()
    case _: TakeSubSystemSnapshot => this.snapshot()
  }

  def orientation(): Unit = {
  }

  def acceleration(): Unit = {

  }

  def gyroscope(): Unit = {

  }

  def compass(): Unit = {

  }

  def temperature(): Unit = {

  }

  def humidity(): Unit = {

  }

  override def remoteSnapshot: Future[Environment] = Futures.successful(null)
}
