package com.salcedo.rapbot.object.jukebox;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public final class JukeBoxes {
    public static JukeBox list(final Path musicLibrary) {
        final List<Path> names = getSongPaths(musicLibrary)
                .distinct()
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".mp3") || file.getName().endsWith(".mp4"))
                .map(File::toPath)
                .collect(toCollection(ArrayList::new));

        Collections.shuffle(names, new SecureRandom());

        if (names.isEmpty()) {
            throw new IllegalStateException("No songs in music library path: " + musicLibrary);
        }

        return new ListJukeBox(names);
    }

    private static Stream<Path> getSongPaths(final Path musicLibrary) {
        try {
            return Files.list(musicLibrary);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
