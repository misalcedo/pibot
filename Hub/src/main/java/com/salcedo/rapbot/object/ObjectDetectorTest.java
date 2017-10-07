package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import com.salcedo.rapbot.vision.VisionService;
import com.salcedo.rapbot.vision.VisionServiceFactory;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public final class ObjectDetectorTest {
    public static void main(final String[] args) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        new ObjectDetectorTest().runV3();
    }

    private void run() {
        System.out.println("\nRunning Detect Demo");
        // Create a detector from the cascade file in the resources directory.
        detectWithClassifier("haarcascade_fullbody");
        detectWithClassifier("haarcascade_lowerbody");
        detectWithClassifier("haarcascade_upperbody");

        detectWithClassifier("haarcascade_frontalface_default");
        detectWithClassifier("haarcascade_frontalface_alt");
        detectWithClassifier("haarcascade_frontalface_alt2");
        detectWithClassifier("haarcascade_frontalface_alt_tree");
    }

    private void runV2() throws Exception {
        System.out.println("\nRunning Detect Demo v2");
        // Create a detector from the cascade file in the resources directory.

        final CascadeClassifier detector = new CascadeClassifier(getPath("haarcascade_frontalface_alt2.xml"));
        final MatOfRect bodyDetections = new MatOfRect();
        final VisionService visionService = VisionServiceFactory.url(ActorSystem.create("RapBot"));
        final JFrame frame = new JFrame("RapBot");
        final JLabel container = new JLabel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 640, 480);
        frame.setVisible(true);
        frame.setContentPane(container);
        container.setVisible(true);
        container.setBounds(100, 100, 640, 480);

        while (true) {
            final Path path = visionService.takePicture()
                    .toCompletableFuture().get(5L, TimeUnit.SECONDS)
                    .toAbsolutePath();
            final Mat image = Imgcodecs.imread(path.toString());

            // Detect faces in the image.
            // MatOfRect is a special container class for Rect.
            detector.detectMultiScale(image, bodyDetections);
            System.out.println(String.format("Detected %s people", bodyDetections.toArray().length));

            // Draw a bounding box around each person.
            for (final Rect rect : bodyDetections.toArray()) {
                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            }
            // Save the visualized detection.
            final String filename = Files.createTempFile("face-detection", ".png").toAbsolutePath().toString();
            System.out.println(String.format("Writing face detection results to %s", filename));
            Imgcodecs.imwrite(filename, image);

            container.setIcon(new ImageIcon(filename));
        }
    }

    private void runV3() throws Exception {
        System.out.println("\nRunning Detect Demo v2");
        // Create a detector from the cascade file in the resources directory.

        final ObjectDetector objectDetector = ObjectDetectors.openCV(ActorSystem.create("RapBot"));

        while (true) {
            System.out.println("object detected: " + objectDetector.objectDetected());
        }
    }

    private void detectWithClassifier(final String classifier) {
        final CascadeClassifier detector = new CascadeClassifier(getPath(classifier + ".xml"));
        detectImages(detector, classifier, "centered");
        detectImages(detector, classifier, "sideways");
        detectImages(detector, classifier, "multiple");
    }

    private void detectImages(final CascadeClassifier detector, final String classifier, final String resource) {
        final Mat image = Imgcodecs.imread(getPath(resource + ".JPG"));
        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        final MatOfRect bodyDetections = new MatOfRect();
        detector.detectMultiScale(image, bodyDetections);
        System.out.println(
                String.format(
                        "Detected %s people for classifier %s resource %s",
                        bodyDetections.toArray().length,
                        classifier,
                        image
                )
        );
        // Draw a bounding box around each person.
        for (final Rect rect : bodyDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }
        // Save the visualized detection.
        final String filename = classifier + "-" + resource + ".png";
        System.out.println(String.format("Writing %s", filename));
        Imgcodecs.imwrite(filename, image);
    }

    private String getPath(final String resource) {
        return Paths.get("/staging", "src", "main", "resources", resource).toString();
    }
}
