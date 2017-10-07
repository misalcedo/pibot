package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import com.salcedo.rapbot.vision.VisionServiceFactory;

public interface ObjectDetectors {
    static ObjectDetector openCV(final ActorSystem system) {
        return new OpenCVObjectDetector(
                "haarcascade_frontalface_alt2.xml",
                VisionServiceFactory.url(system),
                system
        );
    }
}
