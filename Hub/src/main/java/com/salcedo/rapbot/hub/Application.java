package com.salcedo.rapbot.hub;

import akka.actor.ActorSystem;
import akka.actor.Props;

public final class Application {
    public static void main(String[] arguments) throws Exception {
        ActorSystem rapBot = ActorSystem.create("RapBot");

        rapBot.actorOf(Props.create(RapBot.class));
    }
}
