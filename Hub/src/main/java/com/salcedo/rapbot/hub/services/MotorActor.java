package com.salcedo.rapbot.hub.services;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.motor.MotorRequest;
import com.salcedo.rapbot.motor.MotorService;

public final class MotorActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final MotorService motorService;

    public MotorActor(final MotorService motorService) {
        this.motorService = motorService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MotorRequest.class, this::drive)
                .build();
    }

    private void drive(final MotorRequest request) {
        log.info("Received a motor request. Request: {}", request);

        motorService.drive(request);
    }
}
