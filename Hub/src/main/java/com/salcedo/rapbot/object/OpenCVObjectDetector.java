package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.vision.VisionService;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class OpenCVObjectDetector implements ObjectDetector {
    private final LoggingAdapter log;
    private final CascadeClassifier detector;
    private final MatOfRect bodyDetections;
    private final VisionService visionService;

    OpenCVObjectDetector(final String resource, final VisionService visionService, final ActorSystem system) {
        this.visionService = visionService;
        this.detector = new CascadeClassifier(getPath(resource));
        this.bodyDetections = new MatOfRect();
        this.log = Logging.getLogger(system, this);

    }

    @Override
    public boolean objectDetected() {
        final Path path = takePicture();
        final Mat image = Imgcodecs.imread(path.toString());

        this.detector.detectMultiScale(image, this.bodyDetections);
        this.log.info("Detected {} people", this.bodyDetections.toArray().length);

        return this.bodyDetections.toArray().length != 0;
    }

    private Path takePicture() {
        try {
            return this.visionService.takePicture()
                    .toCompletableFuture().get(5L, TimeUnit.SECONDS)
                    .toAbsolutePath();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    private String getPath(final String resource) {
        return Paths.get("/staging", "src", "main", "resources", resource).toString();
    }
}
