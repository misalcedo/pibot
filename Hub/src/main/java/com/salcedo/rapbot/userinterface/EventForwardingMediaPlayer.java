package com.salcedo.rapbot.userinterface;

import akka.actor.ActorRef;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import java.awt.event.KeyEvent;

import static akka.actor.ActorRef.noSender;

public final class EventForwardingMediaPlayer extends EmbeddedMediaPlayerComponent {
    private final ActorRef actor;

    EventForwardingMediaPlayer(final ActorRef actor) {
        super();
        this.actor = actor;
    }

    @Override
    public void keyTyped(final KeyEvent keyEvent) {
        this.actor.tell(keyEvent, noSender());
    }

    @Override
    public void keyPressed(final KeyEvent keyEvent) {
        this.actor.tell(keyEvent, noSender());
    }

    @Override
    public void keyReleased(final KeyEvent keyEvent) {
        this.actor.tell(keyEvent, noSender());
    }
}
