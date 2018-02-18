package com.salcedo.rapbot.hub;

import com.salcedo.rapbot.locomotion.MotorService;
import com.salcedo.rapbot.sense.SenseService;
import com.salcedo.rapbot.vision.VisionService;

public interface ServiceFactory {
    SenseService sense();
    VisionService vision();
    MotorService motor();
}
