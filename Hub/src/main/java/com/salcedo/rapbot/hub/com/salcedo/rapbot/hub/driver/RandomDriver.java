package com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.ActorMaterializer;
import akka.util.ByteString;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public final class RandomDriver extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Random random;
    private final ActorMaterializer materializer;
    private final Http http;

    private RandomDriver() {
        this.random = new SecureRandom();
        this.http = Http.get(getContext().getSystem());
        this.materializer = ActorMaterializer.create(getContext().getSystem());
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

        log.info("Making a drive request to '{}' with speed {} and command {}", driveRequest.getHostname(), speed, command);

        makeRequest(driveRequest.getHostname(), 1, speed, command);
        makeRequest(driveRequest.getHostname(), 2, speed, command);
    }

    private void makeRequest(String hostname, int motorId, int speed, int command) {
        ActorRef sender = getSender();
        HttpRequest httpRequest = HttpRequest.create(getUri(hostname, motorId, speed, command))
                .withMethod(HttpMethods.PUT);

        http.singleRequest(httpRequest, materializer)
                .thenApply(HttpResponse::entity)
                .thenCompose(this::getStrictEntity)
                .thenApply(HttpEntity.Strict::getData)
                .thenApply(ByteString::utf8String)
                .thenAccept(response -> respondToDriveRequest(hostname, response, sender));
    }

    private void respondToDriveRequest(String hostname, String response, ActorRef sender) {
        sender.tell(new DriveResponse(hostname, response), self());
    }

    private CompletionStage<HttpEntity.Strict> getStrictEntity(ResponseEntity responseEntity) {
        return responseEntity.toStrict(TimeUnit.SECONDS.toMillis(1L), materializer);
    }

    private String getUri(String hostname, final int motorId, final int speed, final int command) {
        return hostname + motorId + "?speed=" + speed + "&command=" + command;
    }
}
