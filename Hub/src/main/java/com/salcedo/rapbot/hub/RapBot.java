package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.hub.driver.invoke.DriveRequest;
import com.salcedo.rapbot.hub.driver.random.RandomDriver;
import com.salcedo.rapbot.hub.services.Motors;
import com.salcedo.rapbot.motor.MotorResponse;
import com.salcedo.rapbot.motor.MotorServiceFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public final class RapBot extends AbstractActor {
    private static final FiniteDuration DRIVE_DELAY = Duration.create(1L, TimeUnit.SECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef driver;
    private ActorRef motors;

    @Override
    public void preStart() {
        motors = getContext().actorOf(Props.create(Motors.class, MotorServiceFactory.http(getContext().getSystem())));
        driver = getContext().actorOf(Props.create(RandomDriver.class, motors));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DriveRequest.class, this::forwardDriveRequest)
                .match(MotorResponse.class, response -> logResponse())
                .match(Terminated.class, this::shutdown)
                .build();
    }

    private void shutdown(Terminated terminated) {
        log.error("Driver terminated unexpectedly.", terminated.actor());
        getContext().stop(self());
    }

    private void logResponse() {
        getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        DRIVE_DELAY,
                        driver,
                        new DriveRequest(),
                        getContext().dispatcher(),
                        self()
                );

        log.info("Driver responded to drive request");
    }

    private void forwardDriveRequest(DriveRequest request) {
        driver.tell(request, self());
    }
}
