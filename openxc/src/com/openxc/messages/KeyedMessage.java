package com.openxc.messages;

public abstract class KeyedMessage extends VehicleMessage {
    public KeyedMessage() {
        super();
    }

    public KeyedMessage(Long timestamp) {
        super(timestamp);
    }

    public abstract MessageKey getKey();
}
