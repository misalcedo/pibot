package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.RandomDriver;

public final class RapBot extends AbstractActor {
    private final ActorRef driver;

    public RapBot() {
        this.driver = getContext().actorOf(Props.create(RandomDriver.class));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }
}
