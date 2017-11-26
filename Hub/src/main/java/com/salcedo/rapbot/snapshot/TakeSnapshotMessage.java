package com.salcedo.rapbot.snapshot;

import java.util.UUID;

public class TakeSnapshotMessage {
    private UUID uuid;

    TakeSnapshotMessage(final UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
