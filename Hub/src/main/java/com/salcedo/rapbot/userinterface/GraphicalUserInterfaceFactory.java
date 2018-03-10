package com.salcedo.rapbot.userinterface;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.SystemState;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

public abstract class GraphicalUserInterfaceFactory {
    public static SwingGraphicalUserInterface awt(final ActorSystem system, Uri videoFeed) {
        final EventStreamKeyListener keyListener = new EventStreamKeyListener(system.eventStream());
        return new SwingGraphicalUserInterface(videoFeed, keyListener);
    }

    public static GraphicalUserInterface noop() {
        return new GraphicalUserInterface() {
            @Override
            public void display() {}

            @Override
            public void onClose(final Runnable runnable) {}

            @Override
            public void update(final SystemState state) {}
        };
    }
}
