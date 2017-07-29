package com.salcedo.rapbot.keyboard;

import akka.actor.ActorRef;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;

public class KeyEventSource implements EventSource {
    @Override
    public void listen(ActorRef actor) {
        JFrame frame = new JFrame("RapBot");
        EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyEventForwarder(actor));
        frame.setBounds(100, 100, 640, 480);
        frame.setContentPane(mediaPlayerComponent);
        frame.setVisible(true);

        mediaPlayerComponent.getMediaPlayer().playMedia("http://192.168.1.41:3001/stream.mjpg");
    }
}
