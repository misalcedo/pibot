package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.jukebox.JukeBox;
import com.salcedo.rapbot.jukebox.JukeBoxes;
import com.salcedo.rapbot.vision.VisionServiceFactory;
import org.opencv.core.Core;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.setLookAndFeel;

public final class ObjectDetectorTest extends WindowAdapter {
    private static final String APP_NAME = "FaceDetect";
    private final static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final LoggingAdapter log;
    private final ObjectDetector objectDetector;
    private final JFrame frame;
    private final JukeBox jukeBox;

    private ObjectDetectorTest(final Path musicLibrary) throws Exception {
        final ActorSystem system = ActorSystem.create(APP_NAME);

        this.objectDetector = ObjectDetectors.openCV(VisionServiceFactory.url(system, Uri.create("http://192.169.1.41:3001")), system);
        this.frame = new JFrame(APP_NAME);
        this.log = Logging.getLogger(system, this);
        this.jukeBox = JukeBoxes.create(system, musicLibrary, new AudioMediaPlayerComponent());
    }

    public static void main(final String[] args) throws Exception {
        setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        final Path musicLibrary = Paths.get("/usr", "src", "app", "Music");
        final ObjectDetectorTest objectDetectorTest = new ObjectDetectorTest(musicLibrary);

        invokeLater(objectDetectorTest::initializeFrame);

        objectDetectorTest.run();
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        this.log.info("Terminating application.");
        this.jukeBox.turnOff();
        System.exit(0);
    }

    private void run() {
        this.log.info("Running Face Detect Demo. JukeBox: {}", this.jukeBox);

        executorService.scheduleAtFixedRate(this::detectLoop, 0L, 5L, TimeUnit.SECONDS);
    }

    private void detectLoop() {
        try {
            detectFaceAndPlayMusic();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void detectFaceAndPlayMusic() {
        if (this.jukeBox.isPlayingMusic()) {
            return;
        }

        if (this.objectDetector.objectDetected()) {
            this.log.info("Face detected.");
            this.jukeBox.playNextSong();
        } else {
            this.log.info("No face detected. JukeBox: {}", this.jukeBox);
        }

    }

    private void initializeFrame() {
        final JButton pauseButton = new JButton("Pause/Resume");
        final JButton previousButton = new JButton("Previous");
        final JButton nextButton = new JButton("Next");

        final JPanel controlsPane = new JPanel();
        controlsPane.add(pauseButton);
        controlsPane.add(previousButton);
        controlsPane.add(nextButton);

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        pauseButton.addActionListener(event -> this.jukeBox.pauseOrResume());
        previousButton.addActionListener(event -> this.jukeBox.playPreviousSong());
        nextButton.addActionListener(event -> this.jukeBox.playNextSong());

        this.frame.setContentPane(contentPane);
        this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(this);
        this.frame.pack();
        this.frame.setVisible(true);
    }
}
