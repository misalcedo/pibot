package com.salcedo.rapbot.userinterface;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;

public interface GraphicalUserInterfaceFactory {
    static GraphicalUserInterface awt(final ActorSystem system, final Uri uri) {
        final EventStreamKeyListener keyListener = new EventStreamKeyListener(system.eventStream());
        return new SwingGraphicalUserInterface(uri, keyListener);
    }
}
