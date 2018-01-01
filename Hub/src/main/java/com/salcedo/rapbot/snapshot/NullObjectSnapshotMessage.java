package com.salcedo.rapbot.snapshot;

import java.util.UUID;

public final class NullObjectSnapshotMessage implements SnapshotMessage {
    private final UUID uuid;

    NullObjectSnapshotMessage(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public <T> T getSnapshot(Class<? extends T> type) {
        throw new IllegalArgumentException("No snapshot object found.");
    }
}
