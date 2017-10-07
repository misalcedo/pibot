package com.salcedo.rapbot.vision;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface VisionServiceFactory {
    static VisionService http(final ActorSystem system) {
        return new HttpVisionService(
                Http.get(system),
                ActorMaterializer.create(system),
                Uri.create("http://192.168.1.23:3001/still.jpg")
        );
    }

    static VisionService url(final ActorSystem system) {
        try {
            final URL url = URI.create("http://192.168.1.41:3001/still.jpg").toURL();
            return new UrlVisionService(url, system);
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}