package com.salcedo.rapbot.userinterface;

import akka.actor.ActorRef;

public interface EventSource {
    void listen(ActorRef actor);
}
