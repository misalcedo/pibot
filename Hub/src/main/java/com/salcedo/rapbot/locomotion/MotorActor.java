package com.salcedo.rapbot.locomotion;

import akka.actor.Props;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.hub.ServiceClientActor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public final class MotorActor extends ServiceClientActor {
    private final LoggingAdapter log = Logging.getLogger(this);
    private final MotorService motorService;
    private boolean requestInProgress;
    private Deque<MotorRequest> pendingRequest;

    public MotorActor(final MotorService motorService) {
        this.motorService = motorService;
        this.requestInProgress = false;
        this.pendingRequest = new ArrayDeque<>(1);
    }

    public static Props props(final MotorService motorService) {
        return Props.create(MotorActor.class, motorService);
    }

    @Override
    public void preStart() {
        requestInProgress = false;
        pendingRequest.clear();
        release();
    }

    private void release() {
        motorService.release();
    }

    @Override
    public void postStop() {
        release();
    }

    @Override
    public Receive createReceive() {
        return baseReceiveBuilder()
                .match(MotorRequest.class, this::bufferedDrive)
                .match(Status.Success.class, success -> driveNextRequest())
                .build();
    }

    private void bufferedDrive(final MotorRequest request) {
        if (requestInProgress) {
            pendingRequest.poll();
            pendingRequest.add(request);

            log.debug("Buffered a request.");
        } else {
            drive(request);
        }
    }

    @Override
    protected CompletionStage<?> snapshot() {
        return motorService.state();
    }

    private void driveNextRequest() {
        this.requestInProgress = false;

        Optional.ofNullable(pendingRequest.poll())
                .ifPresent(this::drive);

    }

    private void drive(final MotorRequest request) {
        this.requestInProgress = true;

        callWithBreaker(() -> motorService.drive(request))
                .whenComplete((response, exception) -> self().tell(new Status.Success(0), self()));
    }
}
