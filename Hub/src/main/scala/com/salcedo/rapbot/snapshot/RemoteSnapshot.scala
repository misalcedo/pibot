package com.salcedo.rapbot.snapshot

import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}

import akka.actor.Status.Status
import akka.actor.{Actor, PoisonPill, Status}
import akka.pattern.{CircuitBreaker, pipe}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait RemoteSnapshot extends Actor {
  import context.dispatcher

  val breaker = new CircuitBreaker(
    context.system.scheduler,
    10,
    Duration.create(100, MILLISECONDS),
    Duration.create(1, SECONDS)
  )
  var snapshotFuture: Future[Status] = Future(Status.Success(0))

  breaker.onOpen(self ! PoisonPill)

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
