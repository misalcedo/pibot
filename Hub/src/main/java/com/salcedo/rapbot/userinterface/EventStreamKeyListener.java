package com.salcedo.rapbot.userinterface;

import akka.event.EventStream;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Listens to {@link KeyEvent}s and publishes them to the {@link EventStream}.
 */
public final class EventStreamKeyListener implements KeyListener {
    private final EventStream eventStream;

    public EventStreamKeyListener(final EventStream eventStream) {
        this.eventStream = eventStream;
    }

    @Override
    public void keyTyped(final KeyEvent keyEvent) {
        eventStream.publish(keyEvent);
    }

    @Override
    public void keyPressed(final KeyEvent keyEvent) {
        eventStream.publish(keyEvent);
    }

    @Override
    public void keyReleased(final KeyEvent keyEvent) {
        eventStream.publish(keyEvent);
    }
}
