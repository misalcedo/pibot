package com.salcedo.rapbot

import java.nio.file.Paths
import javax.swing.SwingUtilities.invokeLater

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import com.salcedo.rapbot.driver.DriverActor
import com.salcedo.rapbot.hub.Hub
import com.salcedo.rapbot.hub.Hub.SubSystem
import com.salcedo.rapbot.motor.MotorActor
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory
import com.salcedo.rapbot.vision.VisionActor
import com.salcedo.rapbot.websocket.WebSocketActor
import kamon.Kamon
import kamon.prometheus.PrometheusReporter
import kamon.zipkin.ZipkinReporter

object Application extends App {
  Kamon.addReporter(new PrometheusReporter)
  Kamon.addReporter(new ZipkinReporter)

  val system = ActorSystem("RapBot")

  val robot = Uri("http://192.168.1.41")

  val workingDirectory = Paths.get("/home", "miguel", "IdeaProjects", "RapBot", "data", "test")

  val videoFeed = Uri("http://www.rmp-streaming.com/media/bbb-360p.mp4")
  val ui = GraphicalUserInterfaceFactory.awt(system, akka.http.javadsl.model.Uri.create(videoFeed.toString()))
  val hubProps = Hub.props(
    ui,
    SubSystem(DriverActor.props(), "driver"),
    SubSystem(MotorActor.props(robot.withPort(3000)), "motor"),
    SubSystem(VisionActor.props(robot.withPort(3001), workingDirectory), "vision"),
    SubSystem(WebSocketActor.props(3002), "websocket")
  )

  system.log.info("Starting system with working directory of: {}.", workingDirectory)
  system.actorOf(hubProps, "hub")
  system.registerOnTermination(Kamon.stopAllReporters)

  ui.onClose(new Runnable {
    override def run(): Unit = system.terminate()
  })
  invokeLater(new Runnable {
    override def run(): Unit = ui.display()
  })
}
