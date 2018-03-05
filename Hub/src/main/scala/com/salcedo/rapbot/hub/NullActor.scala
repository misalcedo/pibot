package com.salcedo.rapbot.hub

import akka.actor.{Actor, ActorLogging, Props, Status}
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot

object NullActor {
  def props: Props = Props(new NullActor)
}

class NullActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case _: TakeSubSystemSnapshot => this.snapshot()
  }

  def snapshot(): Unit = {
    sender() ! Status.Failure(new IllegalStateException("No such sub-system."))
  }
}
