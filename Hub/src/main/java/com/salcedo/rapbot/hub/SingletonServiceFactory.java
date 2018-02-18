package com.salcedo.rapbot.hub;

import com.salcedo.rapbot.locomotion.MotorService;
import com.salcedo.rapbot.sense.SenseService;
import com.salcedo.rapbot.vision.VisionService;

public class SingletonServiceFactory implements ServiceFactory {
    private final MotorService motorService;
    private final VisionService visionService;
    private final SenseService senseService;

    public SingletonServiceFactory(MotorService motorService, VisionService visionService, SenseService senseService) {
        this.motorService = motorService;
        this.visionService = visionService;
        this.senseService = senseService;
    }

    @Override
    public SenseService sense() {
        return senseService;
    }

    @Override
    public VisionService vision() {
        return visionService;
    }

    @Override
    public MotorService motor() {
        return motorService;
    }
}
