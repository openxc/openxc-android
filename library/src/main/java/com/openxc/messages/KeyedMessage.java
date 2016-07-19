package com.openxc.messages;

/**
 * A KeyedMessage is a VehicleMessage with a unique key that identifies this
 * message, and potentially responses to this message. For example, the CAN
 * message ID and bus ID form a unique key for a CAN message.
 */
public abstract class KeyedMessage extends VehicleMessage {
    //the transient designation prevents this from being serialized when we write to a tracefile
    private transient MessageKey mKey;

    public KeyedMessage() {
        super();
    }

    public KeyedMessage(Long timestamp) {
        super(timestamp);
    }

    /**
     * Return the identifying key for this message.
     */
    public MessageKey getKey() {
        return mKey;
    }

    protected void setKey(MessageKey key) {
        mKey = key;
    }
}
