package com.salcedo.rapbot.websocket

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, Props, Terminated}
import akka.routing.{BroadcastRoutingLogic, Router}
import com.salcedo.rapbot.snapshot.SnapshotActor.Snapshot
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot

object WebSocketActor {
  def props(port: Int): Props = Props(new WebSocketActor(port))
}

class WebSocketActor(port: Int) extends Actor with ActorLogging {
  var router = Router(BroadcastRoutingLogic())

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Snapshot])
  }

  override def receive: Receive = {
    case _: TakeSubSystemSnapshot => sender() ! Success(None)
    case message: Snapshot => this.snapshot(message)
    case terminated: Terminated => this.terminate(terminated)
  }

  def snapshot(message: Snapshot): Unit = {
    log.info(message.toString)
  }

  private def terminate(message: Terminated): Unit = {
    router = router.removeRoutee(message.actor)
  }
}