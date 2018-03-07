package com.salcedo.rapbot.snapshot

import akka.actor.Status.Status
import akka.actor.{Actor, Status}
import akka.pattern.pipe
import com.salcedo.rapbot.remote.ActorBreaker

import scala.concurrent.Future

trait RemoteSnapshot extends Actor with ActorBreaker {

  import context.dispatcher

  var snapshotFuture: Future[Status] = Future(Status.Success(0))

  def snapshot(): Unit = {
    if (snapshotFuture.isCompleted) {
      snapshotFuture = breaker.withCircuitBreaker(this.remoteSnapshot)
        .map(Status.Success)
        .recover { case e: Throwable => Status.Failure(e) }
    }

    pipe(snapshotFuture).to(sender(), self)
  }

  def remoteSnapshot: Future[Any]
}
