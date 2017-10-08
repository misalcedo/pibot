package com.salcedo.rapbot.userinterface;

import akka.actor.ActorRef;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;

public class GraphicalUserInterface implements EventSource {
    @Override
    public void listen(ActorRef actor) {
        JFrame frame = new JFrame("RapBot");
        EmbeddedMediaPlayerComponent mediaPlayerComponent = new EventForwardingMediaPlayer(actor);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 640, 480);
        frame.setContentPane(mediaPlayerComponent);
        frame.setVisible(true);

        mediaPlayerComponent.getMediaPlayer().playMedia(
                "http://192.168.1.23:3001/stream.mjpg",
                ":network-caching=0"
        );
    }
}