package com.salcedo.rapbot.userinterface;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.net.URI;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class VideoFeedGUI extends BaseGUI {
    private final URI uri;

    VideoFeedGUI(final URI uri, final KeyListener keyListener) {
        super(keyListener);
        this.uri = uri;
    }

    @Override
    protected void initializeFrame(final JFrame frame) {
        final EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        mediaPlayerComponent.getVideoSurface().addKeyListener(getKeyListener());

        frame.setContentPane(mediaPlayerComponent);

        mediaPlayerComponent.getMediaPlayer()
                .playMedia(uri.toString(), ":network-caching=0");
    }
}