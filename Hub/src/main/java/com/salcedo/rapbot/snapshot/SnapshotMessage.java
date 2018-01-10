package com.salcedo.rapbot.snapshot;

import java.util.UUID;

public interface SnapshotMessage {
    UUID getId();

    Object getSnapshot();
}
