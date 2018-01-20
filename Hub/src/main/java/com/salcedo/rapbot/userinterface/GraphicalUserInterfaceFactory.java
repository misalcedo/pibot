package com.salcedo.rapbot.userinterface;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

public interface GraphicalUserInterfaceFactory {
    static SwingGraphicalUserInterface awt(final ActorSystem system, Uri videoFeed) {
        final EventStreamKeyListener keyListener = new EventStreamKeyListener(system.eventStream());
        return new SwingGraphicalUserInterface(videoFeed, keyListener);
    }
}
