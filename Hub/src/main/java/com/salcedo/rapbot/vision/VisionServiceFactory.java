package com.salcedo.rapbot.vision;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;
import uk.co.caprica.vlcj.player.MediaPlayer;

import java.net.MalformedURLException;
import java.net.URL;

public interface VisionServiceFactory {
    static VisionService http(final ActorSystem system, final Uri uri) {
        return new HttpVisionService(Http.get(system), ActorMaterializer.create(system), uri);
    }

    static VisionService url(final ActorSystem system, final Uri uri) {
        try {
            return new UrlVisionService(new URL(uri.toString()), system);
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static VisionService vlcj(MediaPlayer mediaPlayer) {
        final MediaPlayerBackedVisionService visionService = new MediaPlayerBackedVisionService(mediaPlayer);
        mediaPlayer.addMediaPlayerEventListener(visionService);
        return visionService;
    }
}