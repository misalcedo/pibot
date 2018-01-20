package com.salcedo.rapbot.locomotion;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;
import com.google.gson.GsonBuilder;

public interface MotorServiceFactory {
    static MotorService http(final ActorSystem system, final Uri uri) {
        return new HttpMotorService(
                Http.get(system),
                ActorMaterializer.create(system),
                uri,
                new GsonBuilder().create()
        );
    }

    static MotorService stub() {
        return new StubMotorService();
    }
}
