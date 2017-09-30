package com.salcedo.rapbot.userinterface;

import akka.actor.ActorRef;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import java.awt.event.KeyEvent;

import static akka.actor.ActorRef.noSender;

public final class EventForwardingMediaPlayer extends EmbeddedMediaPlayerComponent {
    private final ActorRef actor;

    EventForwardingMediaPlayer(ActorRef actor) {
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
