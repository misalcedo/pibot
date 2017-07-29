package com.salcedo.rapbot.hub.services;

import akka.actor.AbstractActor;
import com.salcedo.rapbot.motor.MotorRequest;
import com.salcedo.rapbot.motor.MotorService;

public final class Motors extends AbstractActor {
    private final MotorService motorService;

    public Motors(MotorService motorService) {
        this.motorService = motorService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MotorRequest.class, this::drive)
                .build();
    }

    private void drive(MotorRequest request) {
        motorService.drive(request);
    }
}
