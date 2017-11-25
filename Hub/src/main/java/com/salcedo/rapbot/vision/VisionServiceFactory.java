package com.salcedo.rapbot.vision;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public interface VisionServiceFactory {
    static VisionService url(final ActorSystem system, final Uri uri) {
        try {
            return new UrlVisionService(new URL(uri.toString()), system);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}