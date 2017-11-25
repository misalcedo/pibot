package com.salcedo.rapbot.snapshot;

import akka.actor.ActorRef;

public final class RegisterSubSystemMessage {
    private final ActorRef subSystem;

    public RegisterSubSystemMessage(ActorRef subSystem) {
        this.subSystem = subSystem;
    }

    public ActorRef getSubSystem() {
        return subSystem;
    }
}
