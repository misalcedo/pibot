package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import org.opencv.core.Core;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.setLookAndFeel;

public final class ObjectDetectorTest {
    private static final String APP_NAME = "FaceDetect";
    private final AtomicBoolean isPlayingMusic;
    private final ObjectDetector objectDetector;
    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent playerComponent;
    private final Path musicLibrary;

    private ObjectDetectorTest(final Path musicLibrary) {
        this.musicLibrary = musicLibrary;
        this.isPlayingMusic = new AtomicBoolean(false);
        this.playerComponent = new EmbeddedMediaPlayerComponent();
        this.objectDetector = ObjectDetectors.openCV(ActorSystem.create(APP_NAME));
        this.frame = new JFrame(APP_NAME);
    }

    public static void main(final String[] args) throws Exception {
        setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        final Path musicLibrary = Paths.get("/usr", "src", "app", "Music");
        final ObjectDetectorTest objectDetectorTest = new ObjectDetectorTest(musicLibrary);
        invokeLater(objectDetectorTest::run);
    }

    private void run() {
        System.out.println("\nRunning Face Detect Demo");

        initializeFrame();

        detectLoop();
    }

    private void detectLoop() {
        while (true) {
            try {
                detectFaceAndPlayMusic();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void detectFaceAndPlayMusic() {
        if (this.isPlayingMusic.get()) {
            sleep();
        } else {
            if (this.objectDetector.objectDetected()) {
                System.out.println("Face detected.");
                playMusic();
            } else {
                System.out.println("No face detected.");
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initializeFrame() {
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setBounds(100, 100, 640, 480);
        this.frame.setVisible(true);
        this.frame.setContentPane(this.playerComponent);
    }

    private void playMusic() {
        this.playerComponent.getMediaPlayer().playMedia(getSong().toString());
        this.isPlayingMusic.set(true);
    }

    private Path getSong() {
        final List<String> names = getSongPaths()
                .distinct()
                .map(Path::toFile)
                .map(File::getName)
                .filter(name -> name.endsWith(".mp3") || name.endsWith(".mp4"))
                .collect(toCollection(ArrayList::new));

        Collections.shuffle(names);

        if (names.isEmpty()) {
            throw new IllegalStateException("No songs in music library path: " + this.musicLibrary);
        }

        return this.musicLibrary.resolve(names.iterator().next());
    }

    private Stream<Path> getSongPaths() {
        try {
            return Files.list(this.musicLibrary);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
