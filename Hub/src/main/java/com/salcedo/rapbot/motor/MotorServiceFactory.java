package com.salcedo.rapbot.motor;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;
import com.google.gson.GsonBuilder;

public interface MotorServiceFactory {
    static MotorService http(ActorSystem system) {
        return new HttpMotorService(
                Http.get(system),
                ActorMaterializer.create(system),
                Uri.create("http://192.168.1.41:3000/motors"),
                new GsonBuilder().create()
        );
    }
}
