package com.salcedo.rapbot.keyboard;

import akka.actor.ActorRef;

public interface EventSource {
    void listen(ActorRef actor);
}
