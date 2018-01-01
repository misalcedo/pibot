package com.salcedo.rapbot.snapshot;

import java.util.UUID;

public interface SnapshotMessage {
    UUID getId();
    <T> T getSnapshot(Class<? extends T> type);
}
