package com.salcedo.rapbot.userinterface;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.salcedo.rapbot.hub.SystemState;

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
        getContext().getSystem().eventStream().subscribe(self(), SystemState.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SystemState.class, gui::update)
                .build();
    }
}
