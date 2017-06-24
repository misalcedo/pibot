package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.DriveRequest;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.DriveResponse;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.RandomDriver;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public final class RapBot extends AbstractActor {
    private static final FiniteDuration DRIVE_DELAY = Duration.create(1L, TimeUnit.SECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef driver;

    @Override
    public void preStart() {
        driver = getContext().actorOf(Props.create(RandomDriver.class));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DriveRequest.class, this::forwardDriveRequest)
                .match(DriveResponse.class, this::processDriveResponse)
                .match(Terminated.class, this::shutdown)
                .build();
    }

    private void shutdown(Terminated terminated) {
        log.error("Driver terminated unexpectedly.", terminated.actor());
        getContext().stop(self());
    }

    private void processDriveResponse(DriveResponse response) {
        getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        DRIVE_DELAY,
                        driver,
                        new DriveRequest(response.getHostname()),
                        getContext().dispatcher(),
                        self()
                );

        log.info("Motor service at {} responded with: {}", response.getHostname(), response.getResponse());
    }

    private void forwardDriveRequest(DriveRequest request) {
        driver.tell(request, self());
    }
}
