package com.salcedo.rapbot.vision;

import java.nio.file.Path;
import java.util.concurrent.CompletionStage;

/**
 * Service for the robot's vision. Provides a video stream and still images.
 */
public interface VisionService {
    /**
     * Takes a still photo, saves the image to local disk and returns the {@link Path} to the image.
     * @return {@link Path} to the image.
     */
    CompletionStage<Path> takePicture();
}
