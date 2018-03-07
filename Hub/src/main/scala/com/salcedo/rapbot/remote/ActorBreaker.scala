package com.salcedo.rapbot.remote

import java.util.concurrent.TimeUnit.{MILLISECONDS, SECONDS}

import akka.actor.{Actor, PoisonPill}
import akka.pattern.CircuitBreaker

import scala.concurrent.duration.Duration

trait ActorBreaker extends Actor{
  val breaker = new CircuitBreaker(
    context.system.scheduler,
    10,
    Duration.create(100, MILLISECONDS),
    Duration.create(1, SECONDS)
  )(context.dispatcher)

  breaker.onOpen(self ! PoisonPill)
}
