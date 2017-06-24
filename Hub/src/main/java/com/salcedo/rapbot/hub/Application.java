package com.salcedo.rapbot.hub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.DriveRequest;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.RandomDriver;

public final class Application {
    public static void main(String[] arguments) throws Exception {
        final ActorSystem system = ActorSystem.create("RapBot");
        ActorRef rapBot = system.actorOf(Props.create(RapBot.class));
        rapBot.tell(new DriveRequest("http://192.168.1.41:3000/motor/"), ActorRef.noSender());
    }
}
