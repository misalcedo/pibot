package com.salcedo.rapbot.object;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.object.jukebox.JukeBox;
import com.salcedo.rapbot.object.jukebox.JukeBoxes;
import org.opencv.core.Core;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.setLookAndFeel;

public final class ObjectDetectorTest extends WindowAdapter {
    private static final String APP_NAME = "FaceDetect";
    private final LoggingAdapter log;
    private final ObjectDetector objectDetector;
    private final JFrame frame;
    private final JukeBox jukeBox;
    private final EmbeddedMediaPlayerComponent playerComponent;

    private ObjectDetectorTest(final Path musicLibrary) {
        final ActorSystem system = ActorSystem.create(APP_NAME);

        this.objectDetector = ObjectDetectors.openCV(system);
        this.frame = new JFrame(APP_NAME);
        this.log = Logging.getLogger(system, this);
        this.playerComponent = new EmbeddedMediaPlayerComponent();
        this.jukeBox = JukeBoxes.create(system, musicLibrary, this.playerComponent);
    }

    public static void main(final String[] args) throws Exception {
        setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        final Path musicLibrary = Paths.get("/usr", "src", "app", "Music");
        final ObjectDetectorTest objectDetectorTest = new ObjectDetectorTest(musicLibrary);
        invokeLater(objectDetectorTest::run);
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        this.log.info("Terminating application.");
        this.jukeBox.turnOff();
        System.exit(0);
    }

    private void run() {
        this.log.info("Running Face Detect Demo. JukeBox: {}", this.jukeBox);

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
        if (this.jukeBox.isPlayingMusic()) {
            sleep();
        } else {
            if (this.objectDetector.objectDetected()) {
                this.log.info("Face detected.");
                this.jukeBox.playNextSong();
            } else {
                this.log.info("No face detected. JukeBox: {}", this.jukeBox);
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
        this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.frame.setBounds(100, 100, 600, 400);
        this.frame.setVisible(true);
        this.frame.addWindowListener(this);

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.playerComponent, BorderLayout.CENTER);

        final JPanel controlsPane = new JPanel();
        final JButton pauseButton = new JButton("Pause/Resume");
        final JButton previousButton = new JButton("Previous");
        final JButton nextButton = new JButton("Next");

        pauseButton.addActionListener(event -> this.jukeBox.pauseOrResume());
        previousButton.addActionListener(event -> this.jukeBox.playPreviousSong());
        nextButton.addActionListener(event -> this.jukeBox.playNextSong());

        controlsPane.add(pauseButton);
        controlsPane.add(previousButton);
        controlsPane.add(nextButton);
        contentPane.add(controlsPane);

        this.frame.setContentPane(contentPane);
    }
}
