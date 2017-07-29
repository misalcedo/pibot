package com.salcedo.rapbot.keyboard;

import akka.actor.ActorRef;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static akka.actor.ActorRef.noSender;

public final class KeyEventForwarder implements KeyListener {
    private final ActorRef actor;

    KeyEventForwarder(ActorRef actor) {
        this.actor = actor;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        actor.tell(keyEvent, noSender());
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        actor.tell(keyEvent, noSender());
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        actor.tell(keyEvent, noSender());
    }
}
