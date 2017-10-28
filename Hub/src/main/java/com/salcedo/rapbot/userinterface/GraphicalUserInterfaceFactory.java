package com.salcedo.rapbot.userinterface;

import akka.actor.ActorSystem;

import java.net.URI;

public interface GraphicalUserInterfaceFactory {
    static GraphicalUserInterface keyboard(final ActorSystem system) {
        return new KeyboardControllerGUI(new EventStreamKeyListener(system.eventStream()));
    }

    static GraphicalUserInterface video(final ActorSystem system) {
        return new VideoFeedGUI(
                URI.create("http://192.168.1.23:3001/stream.mjpg"),
                new EventStreamKeyListener(system.eventStream())
        );
    }
}
