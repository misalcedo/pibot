package com.salcedo.rapbot.object;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.nio.file.Paths;

public final class ObjectDetector {
    public static void main( String[] args )
    {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
        System.out.println( "mat = " + mat.dump() );

        new ObjectDetector().run();
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

    private void detectWithClassifier(String classifier) {
        CascadeClassifier detector = new CascadeClassifier(getPath(classifier + ".xml"));
        detectImages(detector, classifier, "centered");
        detectImages(detector, classifier, "sideways");
        detectImages(detector, classifier, "multiple");
    }

    private void detectImages(CascadeClassifier detector, String classifier, String resource) {
        Mat image = Imgcodecs.imread(getPath(resource + ".JPG"));
        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        MatOfRect bodyDetections = new MatOfRect();
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
        for (Rect rect : bodyDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
        }
        // Save the visualized detection.
        String filename = classifier + "-" + resource + ".png";
        System.out.println(String.format("Writing %s", filename));
        Imgcodecs.imwrite(filename, image);
    }

    private String getPath(String resource) {
        return Paths.get("/staging", "src", "main", "resources", resource).toString();
    }
}
