package com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ResponseEntity;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import scala.concurrent.duration.Duration;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public final class RandomDriver extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Random random;
    private final ActorMaterializer materializer;
    private final Http http;

    public RandomDriver() {
        this.random = new SecureRandom();
        this.http = Http.get(getContext().getSystem());
        this.materializer = ActorMaterializer.create(getContext().getSystem());

        getContext().getSystem().scheduler()
                .schedule(
                        Duration.Zero(),
                        Duration.create(100L, TimeUnit.MILLISECONDS),
                        getSelf(),
                        new DriveRequest("http://motor-service/motor/"),
                        getContext().dispatcher(),
                        ActorRef.noSender()
                );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DriveRequest.class, this::drive)
                .build();
    }

    private void drive(DriveRequest driveRequest) {
        final int speed = random.nextInt(256);
        final int command = random.nextInt(2) + 1;

        makeRequest(driveRequest.getHostname(), 1, speed, command);
        makeRequest(driveRequest.getHostname(), 2, speed, command);
    }

    private void makeRequest(String hostname, int motorId, int speed, int command) {
        HttpRequest httpRequest = HttpRequest.create(getUri(hostname, motorId, speed, command));

        http.singleRequest(httpRequest, materializer)
                .thenApply(HttpResponse::entity)
                .thenApply(ResponseEntity::getDataBytes)
                .thenApply(Source::toString)
                .thenAccept(log::debug);
    }

    private String getUri(String hostname, final int motorId, final int speed, final int command) {
        return hostname + motorId + "?speed=" + speed + "&command=" + command;
    }

    private static final class DriveRequest {
        private final String hostname;

        private DriveRequest(String hostname) {
            this.hostname = hostname;
        }

        String getHostname() {
            return hostname;
        }
    }
}
