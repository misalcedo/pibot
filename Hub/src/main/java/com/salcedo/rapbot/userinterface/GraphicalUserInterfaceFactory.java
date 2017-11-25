package com.salcedo.rapbot.userinterface;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;

import java.net.URI;

public interface GraphicalUserInterfaceFactory {
    static GraphicalUserInterface keyboard(final ActorSystem system) {
        return new KeyboardControllerGUI(new EventStreamKeyListener(system.eventStream()));
    }

    static GraphicalUserInterface video(final ActorSystem system, final Uri uri) {
        return new VideoFeedGUI(
                uri,
                new EventStreamKeyListener(system.eventStream())
        );
    }
}
