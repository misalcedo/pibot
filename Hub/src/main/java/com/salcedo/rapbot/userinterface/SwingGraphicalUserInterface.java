package com.salcedo.rapbot.userinterface;

import akka.http.javadsl.model.Uri;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

import static java.awt.BorderLayout.LINE_END;
import static java.awt.BorderLayout.LINE_START;
import static java.awt.BorderLayout.PAGE_START;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class SwingGraphicalUserInterface implements GraphicalUserInterface {
    private final Uri videoFeed;
    private final JFrame frame;
    private final KeyListener keyListener;

    SwingGraphicalUserInterface(final Uri videoFeed, final KeyListener keyListener) {
        this.videoFeed = videoFeed;
        this.keyListener = keyListener;
        this.frame = new JFrame("RapBot");
    }

    @Override
    public void display() {
        prepareFrame();

        final EmbeddedMediaPlayer embeddedMediaPlayer = setContent();

        finalizeFrame(embeddedMediaPlayer);
    }

    private EmbeddedMediaPlayer setContent() {
        final MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
        final EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        final Canvas videoSurface = createVideoFeed(embeddedMediaPlayer, mediaPlayerFactory);

        frame.add(videoSurface, PAGE_START);
        frame.add(createRotation(), LINE_START);
        frame.add(createThrottle(), LINE_END);

        return embeddedMediaPlayer;
    }

    private Component createThrottle() {
        final JPanel panel = new JPanel();
        final JLabel fieldLabel = new JLabel("Throttle: ");
        final JLabel valueLabel = new JLabel();
        final JSlider slider = new JSlider(0, 100);

        slider.setMinorTickSpacing(10);
        slider.setMajorTickSpacing(20);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(changeEvent -> valueLabel.setText(String.valueOf(slider.getValue())));
        slider.setValue(0);

        panel.add(fieldLabel);
        panel.add(valueLabel);
        panel.add(slider);

        return panel;
    }

    private Component createRotation() {
        final JPanel panel = new JPanel();
        final JLabel fieldLabel = new JLabel("Rotation: ");
        final JLabel valueLabel = new JLabel();
        final JSlider slider = new JSlider(0, 360);

        slider.setMinorTickSpacing(45);
        slider.setMajorTickSpacing(90);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(changeEvent -> valueLabel.setText(String.valueOf(slider.getValue())));
        slider.addChangeListener(changeEvent -> valueLabel.setText(String.valueOf(slider.getValue())));
        slider.setValue(0);

        panel.add(fieldLabel);
        panel.add(valueLabel);
        panel.add(slider);

        return panel;
    }

    private void finalizeFrame(EmbeddedMediaPlayer embeddedMediaPlayer) {
        frame.pack();
        frame.setVisible(true);

        embeddedMediaPlayer.playMedia(this.videoFeed.toString(), ":network-caching=0");
    }

    private void prepareFrame() {
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 800, 600);
        frame.addKeyListener(keyListener);
        frame.setLayout(new BorderLayout());
    }

    private Canvas createVideoFeed(final EmbeddedMediaPlayer embeddedMediaPlayer, final MediaPlayerFactory mediaPlayerFactory) {
        final Canvas canvas = new Canvas();
        final CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);

        canvas.setBackground(Color.BLACK);
        canvas.setSize(640, 480);
        canvas.setVisible(true);

        embeddedMediaPlayer.setVideoSurface(videoSurface);

        return canvas;
    }

    @Override
    public void update(SystemState state) {

    }
}
