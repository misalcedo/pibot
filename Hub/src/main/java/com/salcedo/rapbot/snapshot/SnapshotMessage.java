package com.salcedo.rapbot.snapshot;

import java.util.UUID;

public class SnapshotMessage {
    private final UUID uuid;
    private final Object object;

    public SnapshotMessage(final UUID uuid, final Object object) {
        this.uuid = uuid;
        this.object = object;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "SnapshotMessage{" +
                "uuid=" + uuid +
                ", object=" + object +
                '}';
    }
}
