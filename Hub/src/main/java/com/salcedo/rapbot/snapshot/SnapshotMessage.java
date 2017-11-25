package com.salcedo.rapbot.snapshot;

public class SnapshotMessage {
    private final Object object;

    public SnapshotMessage(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return "SnapshotMessage{" +
                "object=" + object +
                '}';
    }
}
