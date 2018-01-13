package com.salcedo.rapbot.userinterface;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.salcedo.rapbot.snapshot.SystemSnapshot;

public class GraphicalUserInterfaceActor extends AbstractActor {
    private final GraphicalUserInterface gui;

    public GraphicalUserInterfaceActor(GraphicalUserInterface gui) {
        this.gui = gui;
    }

    public static Props props(final GraphicalUserInterface gui) {
        return Props.create(GraphicalUserInterfaceActor.class, gui);
    }

    @Override
    public void preStart() {
        getContext().getSystem().eventStream().subscribe(self(), SystemSnapshot.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SystemSnapshot.class, this::update)
                .build();
    }

    private void update(SystemSnapshot systemSnapshot) {
        final SystemState systemState = new SnapshotBackedSystemState(systemSnapshot, context());
        gui.update(systemState);
    }
}
