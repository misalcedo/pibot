package com.salcedo.rapbot.userinterface;

import akka.http.javadsl.model.Uri;
import uk.co.caprica.vlcj.binding.internal.libvlc_log_level_e;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.awt.BorderLayout.*;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.SwingConstants.VERTICAL;

public class SwingGraphicalUserInterface extends WindowAdapter implements GraphicalUserInterface {
    private final Uri videoFeed;
    private final JFrame frame;
    private final KeyListener keyListener;
    private final JProgressBar throttle;
    private final JProgressBar targetOrientation;
    private final JProgressBar actualOrientation;
    private final JLabel snapshotId;
    private final JLabel snapshotDuration;
    private final JLabel snapshotSubsystems;
    private final JLabel snapshotCompletedSubsystems;
    private final JLabel fullSensorOrientation;
    private final JLabel acceleration;
    private final JLabel leftMotor;
    private final JLabel rightMotor;
    private final JLabel picture;
    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer mediaPlayer;

    SwingGraphicalUserInterface(final Uri videoFeed, final KeyListener keyListener) {
        this.videoFeed = videoFeed;
        this.keyListener = keyListener;
        this.frame = new JFrame("RapBot");
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        this.targetOrientation = new JProgressBar();
        this.actualOrientation = new JProgressBar();
        this.throttle = new JProgressBar();
        this.snapshotId = new JLabel();
        this.snapshotDuration = new JLabel();
        this.snapshotSubsystems = new JLabel();
        this.snapshotCompletedSubsystems = new JLabel();
        this.fullSensorOrientation = new JLabel();
        this.acceleration = new JLabel();
        this.leftMotor = new JLabel();
        this.rightMotor = new JLabel();
        this.picture = new JLabel();
    }

    @Override
    public void display() {
        prepareFrame();
        setContent();
        finalizeFrame();
    }

    private void prepareFrame() {
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.addKeyListener(keyListener);
        frame.setLayout(new BorderLayout());
    }

    private void setContent() {
        frame.add(createVideoFeed(), PAGE_START);
        frame.add(createActualOrientation(), LINE_START);
        frame.add(createTargetOrientation(), CENTER);
        frame.add(createThrottle(), LINE_END);
        frame.add(createSnapshotInfo(), PAGE_END);
    }

    private Component createSnapshotInfo() {
        final JPanel panel = new JPanel(new GridLayout(0, 2));

        panel.add(new JLabel("SystemSnapshot ID: "));
        panel.add(snapshotId);
        panel.add(new JLabel("Duration (milliseconds): "));
        panel.add(snapshotDuration);
        panel.add(new JLabel("Subsystems: "));
        panel.add(snapshotSubsystems);
        panel.add(new JLabel("Completed Subsystems: "));
        panel.add(snapshotCompletedSubsystems);
        panel.add(new JLabel("Sensor Orientation: "));
        panel.add(fullSensorOrientation);
        panel.add(new JLabel("Acceleration (centimeters/second^2): "));
        panel.add(acceleration);
        panel.add(new JLabel("Left Motor: "));
        panel.add(leftMotor);
        panel.add(new JLabel("Right Motor: "));
        panel.add(rightMotor);
        panel.add(new JLabel("Image Path: "));
        panel.add(picture);

        return panel;
    }

    private Canvas createVideoFeed() {
        final Canvas canvas = new Canvas();
        final CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);

        canvas.setFocusable(false);
        canvas.setBackground(Color.BLACK);
        canvas.setSize(640, 480);
        canvas.setVisible(true);

        mediaPlayer.setVideoSurface(videoSurface);

        return canvas;
    }

    private Component createTargetOrientation() {
        return createOrientation(targetOrientation, RED);
    }

    private Component createActualOrientation() {
        return createOrientation(actualOrientation, GREEN);
    }

    private Component createOrientation(final JProgressBar progressBar, final Color color) {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        final JPanel label = new JPanel();
        final JLabel value = new JLabel("0");
        label.add(new JLabel("Orientation: "));
        label.add(value);

        progressBar.setForeground(color);
        progressBar.setMaximum(360);
        progressBar.setBorderPainted(false);
        progressBar.setUI(new CircularProgressBarUI());
        progressBar.addChangeListener(changeEvent -> value.setText(String.valueOf(progressBar.getValue())));
        progressBar.setValue(0);

        panel.add(progressBar);
        panel.add(label);

        return panel;
    }

    private Component createThrottle() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        final JPanel label = new JPanel();
        final JLabel value = new JLabel();
        label.add(new JLabel("Throttle: "));
        label.add(value);

        throttle.setForeground(RED);
        throttle.setMaximum(255);
        throttle.setOrientation(VERTICAL);
        throttle.addChangeListener(changeEvent -> value.setText(String.valueOf(throttle.getValue())));

        panel.add(throttle);
        panel.add(label);

        return panel;
    }

    private void finalizeFrame() {
        frame.pack();
        frame.setVisible(true);
        frame.requestFocus();

        mediaPlayer.setPlaySubItems(true);
        mediaPlayer.playMedia(videoFeed.toString(), ":network-caching=0");
    }

    @Override
    public void update(SystemState state) {
        updateSnapshot(state);
        updateSensors(state);
        updateMotors(state);
        updateDriver(state);
        updateVision(state);
    }

    private void updateVision(SystemState state) {
        picture.setText(state.getImagePath());
    }

    private void updateSnapshot(SystemState state) {
        snapshotId.setText(state.getSnapshotId());
        snapshotDuration.setText(state.getSnapshotDuration());
        snapshotSubsystems.setText(state.getSnapshotSubsystems());
        snapshotCompletedSubsystems.setText(state.getCompletedSnapshotSubsystems());
    }

    private void updateSensors(SystemState state) {
        fullSensorOrientation.setText(state.get3DOrientation());
        acceleration.setText(state.get3DAcceleration());
        actualOrientation.setValue(state.actualOrientation());
    }

    private void updateDriver(SystemState state) {
        throttle.setValue(state.throttle());
        targetOrientation.setValue(state.targetOrientation());
    }

    private void updateMotors(SystemState state) {
        leftMotor.setText(state.getLeftMotorState());
        rightMotor.setText(state.getRightMotorState());
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        mediaPlayer.release();
        mediaPlayerFactory.release();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
