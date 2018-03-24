package com.salcedo.rapbot

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import com.salcedo.rapbot.driver.DriverActor
import com.salcedo.rapbot.hub.{Hub, SystemStateWriterActor}
import com.salcedo.rapbot.hub.Hub.SubSystem
import com.salcedo.rapbot.motor.MotorActor
import com.salcedo.rapbot.vision.VisionActor
import com.salcedo.rapbot.websocket.WebSocketActor

object Application extends App {
  val system = ActorSystem("RapBot")

  val robot = Uri("http://192.168.1.41")
  val workingDirectory = Paths.get("/home", "miguel", "IdeaProjects", "RapBot", "data", "test")

  val hubProps = Hub.props(
    SubSystem(DriverActor.props(), "driver"),
    SubSystem(MotorActor.props(robot.withPort(3000)), "motor"),
    SubSystem(VisionActor.props(robot.withPort(3001), workingDirectory), "vision"),
    SubSystem(WebSocketActor.props(3002), "websocket"),
    SubSystem(SystemStateWriterActor.props(workingDirectory), "writer")
  )

  system.log.info("Starting system with working directory of: {}.", workingDirectory)
  system.actorOf(hubProps, "hub")
}
