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
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.setLookAndFeel;

public final class ObjectDetectorTest extends WindowAdapter {
    private static final String APP_NAME = "FaceDetect";
    private final LoggingAdapter log;
    private final AtomicBoolean isPlayingMusic;
    private final ObjectDetector objectDetector;
    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent playerComponent;
    private final JukeBox jukeBox;

    private ObjectDetectorTest(final Path musicLibrary) {
        final ActorSystem system = ActorSystem.create(APP_NAME);

        this.isPlayingMusic = new AtomicBoolean(false);
        this.playerComponent = new EmbeddedMediaPlayerComponent();
        this.objectDetector = ObjectDetectors.openCV(system);
        this.frame = new JFrame(APP_NAME);
        this.log = Logging.getLogger(system, this);
        this.jukeBox = JukeBoxes.list(musicLibrary);
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
        this.playerComponent.release();
        System.exit(0);
    }

    private void run() {
        this.log.info("Running Face Detect Demo");

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
                this.log.info("Face detected.");
                playMusic(this.jukeBox.next());
            } else {
                this.log.info("No face detected.");
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
        this.frame.setBounds(100, 100, 640, 480);
        this.frame.setVisible(true);
        this.frame.addWindowListener(this);

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.playerComponent, BorderLayout.CENTER);

        final JPanel controlsPane = new JPanel();
        final JButton pauseButton = new JButton("Pause/Resume");
        final JButton previousButton = new JButton("Previous");
        final JButton nextButton = new JButton("Next");

        pauseButton.addActionListener(event -> pauseMusic());
        previousButton.addActionListener(event -> playMusic(this.jukeBox.previous()));
        nextButton.addActionListener(event -> playMusic(this.jukeBox.next()));

        controlsPane.add(pauseButton);
        controlsPane.add(previousButton);
        controlsPane.add(nextButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        this.frame.setContentPane(contentPane);
    }

    private void pauseMusic() {
        if (this.isPlayingMusic.get()) {
            this.log.info("Paused music.");
            this.playerComponent.getMediaPlayer().pause();
            this.isPlayingMusic.set(false);
        } else {
            this.log.info("Resumed music.");
            this.playerComponent.getMediaPlayer().play();
            this.isPlayingMusic.set(true);
        }
    }

    private void playMusic(final Path song) {
        this.log.info("Playing song {}", song);
        this.playerComponent.getMediaPlayer().playMedia(song.toString());
        this.isPlayingMusic.set(true);
    }
}
