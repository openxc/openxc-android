package com.openxc.messages;

public abstract class KeyedMessage extends VehicleMessage {
    private MessageKey mKey;

    public KeyedMessage() {
        super();
    }

    public KeyedMessage(Long timestamp) {
        super(timestamp);
    }

    public MessageKey getKey() {
        return mKey;
    }

    protected void setKey(MessageKey key) {
        mKey = key;
    }
}
