package com.salcedo.rapbot.websocket

import java.net.InetSocketAddress

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, Props, Terminated}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.routing.{BroadcastRoutingLogic, Router}
import com.salcedo.rapbot.hub.Hub.SystemState
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot

object WebSocketActor {
  def props(port: Int): Props = Props(new WebSocketActor(port))
}

class WebSocketActor(port: Int) extends Actor with ActorLogging {
  var router = Router(BroadcastRoutingLogic())

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[SystemState])
    IO(Tcp)(context.system) ! Bind(self, new InetSocketAddress(port))
  }

  override def receive: Receive = {
    case Bound(_) ⇒ log.info(s"Started WebSocket server on http://localhost:$port/.")
    case CommandFailed(_: Bind) => context.stop(self)
    case connected: Connected => this.connect(connected)
    case state: SystemState => this.broadcast(state)
    case terminated: Terminated => this.terminate(terminated)
    case _: TakeSubSystemSnapshot => sender() ! Success(None)
  }

  def connect(connected: Connected): Unit = {
    val routee = context.actorOf(WebSocketConnectionActor.props(sender()))

    context.watch(routee)
    router = router.addRoutee(routee)

    log.debug(s"Received connection from ${connected.remoteAddress}.")

    sender() ! Register(routee)
  }

  def broadcast(state: SystemState): Unit = {
    if (router.routees.nonEmpty) router.route(state, sender())
  }

  private def terminate(message: Terminated): Unit = {
    router = router.removeRoutee(message.actor)
  }
}