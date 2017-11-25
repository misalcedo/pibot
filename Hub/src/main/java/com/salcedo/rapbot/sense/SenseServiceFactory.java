package com.salcedo.rapbot.sense;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;
import com.google.gson.GsonBuilder;

public interface SenseServiceFactory {
    static SenseService http(final ActorSystem system, final Uri uri) {
        return new HttpSenseService(
                Http.get(system),
                ActorMaterializer.create(system),
                uri,
                new GsonBuilder().create()
        );
    }
}
