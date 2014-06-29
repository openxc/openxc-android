package com.openxc.sinks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.openxc.messages.MessageKey;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.VehicleMessage;

/**
 * A common parent class for all vehicle data sinks.
 *
 * Many sinks require a reference to last known value of all keyed messages.
 * This class encapsulates the functionality require to store a reference to the
 * message data structure and query it for values.
 */
public class BaseVehicleDataSink implements VehicleDataSink {
    private Map<MessageKey, KeyedMessage> mKeyedMessages =
            new ConcurrentHashMap<>();

    /**
     * Receive a message.
     *
     * Children of this class can call super.receive() if they need to store
     * copies of received keyed messages to access via the get(String) method.
     */
    public void receive(VehicleMessage message) throws DataSinkException {
        if(message instanceof KeyedMessage) {
            KeyedMessage keyedMessage = (KeyedMessage) message;
            mKeyedMessages.put(keyedMessage.getKey(), keyedMessage);
        }
    }

    public boolean containsKey(MessageKey key) {
        return mKeyedMessages.containsKey(key);
    }

    public KeyedMessage get(MessageKey key) {
        return mKeyedMessages.get(key);
    }

    public Set<Map.Entry<MessageKey, KeyedMessage>> getKeyedMessages() {
        return mKeyedMessages.entrySet();
    }

    public void stop() {
        // do nothing unless you need it
    }
}
