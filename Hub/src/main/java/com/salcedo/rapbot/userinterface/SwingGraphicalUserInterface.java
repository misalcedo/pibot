package com.salcedo.rapbot.userinterface;

import akka.http.javadsl.model.Uri;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

import static java.awt.BorderLayout.*;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import static javax.swing.SwingConstants.VERTICAL;

public class SwingGraphicalUserInterface implements GraphicalUserInterface {
    private final Uri videoFeed;
    private final JFrame frame;
    private final KeyListener keyListener;
    private final JProgressBar throttle;
    private final JProgressBar orientation;
    private final JLabel snapshotId;
    private final JLabel snapshotStart;
    private final JLabel snapshotEnd;

    SwingGraphicalUserInterface(final Uri videoFeed, final KeyListener keyListener) {
        this.videoFeed = videoFeed;
        this.keyListener = keyListener;
        this.frame = new JFrame("RapBot");
        this.orientation = new JProgressBar();
        this.throttle = new JProgressBar();
        this.snapshotId = new JLabel();
        this.snapshotStart = new JLabel();
        this.snapshotEnd = new JLabel();
    }

    @Override
    public void display() {
        prepareFrame();

        final EmbeddedMediaPlayer embeddedMediaPlayer = setContent();

        finalizeFrame(embeddedMediaPlayer);
    }

    private void prepareFrame() {
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.addKeyListener(keyListener);
        frame.setLayout(new BorderLayout());
    }

    private EmbeddedMediaPlayer setContent() {
        final MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
        final EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        final Canvas videoSurface = createVideoFeed(embeddedMediaPlayer, mediaPlayerFactory);

        frame.add(videoSurface, PAGE_START);
        frame.add(createSnapshotInfo(), LINE_START);
        frame.add(createOrientation(), CENTER);
        frame.add(createThrottle(), LINE_END);

        return embeddedMediaPlayer;
    }

    private Component createSnapshotInfo() {
        final JPanel panel = new JPanel(new GridLayout(0, 2));

        panel.add(new JLabel("Snapshot ID: "));
        panel.add(snapshotId);
        panel.add(new JLabel("Start Time: "));
        panel.add(snapshotStart);
        panel.add(new JLabel("End Time: "));
        panel.add(snapshotEnd);

        return panel;
    }

    private Canvas createVideoFeed(final EmbeddedMediaPlayer embeddedMediaPlayer, final MediaPlayerFactory mediaPlayerFactory) {
        final Canvas canvas = new Canvas();
        final CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);

        canvas.setFocusable(false);
        canvas.setBackground(Color.BLACK);
        canvas.setSize(640, 480);
        canvas.setVisible(true);

        embeddedMediaPlayer.setVideoSurface(videoSurface);

        return canvas;
    }

    private Component createOrientation() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        final JPanel label = new JPanel();
        final JLabel value = new JLabel();
        label.add(new JLabel("Orientation: "));
        label.add(value);

        orientation.setMaximum(360);
        orientation.setBorderPainted(false);
        orientation.setUI(new CircularProgressBarUI());
        orientation.addChangeListener(changeEvent -> value.setText(String.valueOf(orientation.getValue())));
        orientation.setValue(0);

        panel.add(orientation);
        panel.add(label);

        return panel;
    }

    private Component createThrottle() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        final JPanel label = new JPanel();
        final JLabel value = new JLabel();
        label.add(new JLabel("Throttle: "));
        label.add(value);

        throttle.setOrientation(VERTICAL);
        throttle.addChangeListener(changeEvent -> value.setText(String.valueOf(throttle.getValue())));

        panel.add(throttle);
        panel.add(label);

        return panel;
    }

    private void finalizeFrame(EmbeddedMediaPlayer embeddedMediaPlayer) {
        frame.pack();
        frame.setVisible(true);
        frame.requestFocus();

        embeddedMediaPlayer.playMedia(this.videoFeed.toString(), ":network-caching=0");
    }

    @Override
    public void update(SystemState state) {
        snapshotStart.setText(state.getSnapshotStart());
        snapshotEnd.setText(state.getSnapshotEnd());
        throttle.setValue(state.throttle());
        orientation.setValue(state.targetOrientation());
    }
}
