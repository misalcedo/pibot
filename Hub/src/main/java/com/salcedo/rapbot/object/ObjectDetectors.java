package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import com.salcedo.rapbot.vision.VisionService;
import com.salcedo.rapbot.vision.VisionServiceFactory;

public interface ObjectDetectors {
    static ObjectDetector openCV(final VisionService visionService, final ActorSystem system) {
        return new OpenCVObjectDetector(
                "haarcascade_frontalface_alt2.xml",
                visionService,
                system
        );
    }
}
