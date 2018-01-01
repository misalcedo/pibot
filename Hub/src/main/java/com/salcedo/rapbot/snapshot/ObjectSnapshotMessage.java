package com.salcedo.rapbot.snapshot;

import java.util.UUID;

public final class ObjectSnapshotMessage implements SnapshotMessage {
    private final UUID uuid;
    private final Object object;

    public ObjectSnapshotMessage(final UUID uuid, final Object object) {
        this.uuid = uuid;
        this.object = object;
    }

    public UUID getId() {
        return uuid;
    }

    public <T> T getSnapshot(Class<? extends T> type) {
        return type.cast(object);
    }

    @Override
    public String toString() {
        return "ObjectSnapshotMessage{" +
                "uuid=" + uuid +
                ", object=" + object +
                '}';
    }
}
