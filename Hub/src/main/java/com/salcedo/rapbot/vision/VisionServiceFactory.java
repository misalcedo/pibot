package com.salcedo.rapbot.vision;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public interface VisionServiceFactory {
    static VisionService url(final URL url, final ActorSystem system) {
        return new UrlVisionService(url, system);
    }
}