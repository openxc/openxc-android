package com.openxc.sinks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.VehicleMessage;

/**
 * A common parent class for all vehicle data sinks.
 *
 * Many sinks require a reference to last known value of all named messages.
 * This class encapsulates the functionality require to store a reference to the
 * message data structure and query it for values.
 */
public class BaseVehicleDataSink implements VehicleDataSink {
    private Map<String, NamedVehicleMessage> mNamedMessages =
            new ConcurrentHashMap<String, NamedVehicleMessage>();

    /**
     * Receive a message, deserialized to primatives.
     *
     * Children of this class can call super.receive() if they need to store
     * copies of received named messages to access via the get(String) method.
     */
    public boolean receive(VehicleMessage message) throws DataSinkException {
        if(message instanceof NamedVehicleMessage) {
            NamedVehicleMessage namedMessage = (NamedVehicleMessage) message;
            mNamedMessages.put(namedMessage.getName(), namedMessage);
        }
        return true;
    }

    public boolean containsNamedMessage(String name) {
        return mNamedMessages.containsKey(name);
    }

    public NamedVehicleMessage getNamedMessage(String name) {
        return mNamedMessages.get(name);
    }

    public Set<Map.Entry<String, NamedVehicleMessage>> getNamedMessages() {
        return mNamedMessages.entrySet();
    }

    public void stop() {
        // do nothing unless you need it
    }
}
