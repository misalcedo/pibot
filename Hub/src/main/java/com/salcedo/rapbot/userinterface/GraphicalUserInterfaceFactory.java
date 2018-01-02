package com.salcedo.rapbot.userinterface;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;

public interface GraphicalUserInterfaceFactory {
    static GraphicalUserInterface keyboard(final ActorSystem system, final Uri uri) {
        final EventStreamKeyListener keyListener = new EventStreamKeyListener(system.eventStream());
        return new SwingGraphicalUserInterface(uri, keyListener);
    }

    static GraphicalUserInterface video(final ActorSystem system, final Uri uri) {
        return new VideoFeedGUI(
                uri,
                new EventStreamKeyListener(system.eventStream())
        );
    }
}
