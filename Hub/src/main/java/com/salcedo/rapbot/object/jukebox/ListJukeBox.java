package com.salcedo.rapbot.object.jukebox;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ListJukeBox implements JukeBox {
    private final AtomicInteger index;
    private final List<Path> songNames;

    ListJukeBox(final List<Path> songNames) {
        this.songNames = songNames;
        this.index = new AtomicInteger(-1);
    }

    @Override
    public Path next() {
        this.index.compareAndSet(this.songNames.size(), -1);
        return this.songNames.get(this.index.incrementAndGet());
    }

    @Override
    public Path previous() {
        this.index.compareAndSet(0, this.songNames.size());
        return this.songNames.get(this.index.decrementAndGet());
    }
}
