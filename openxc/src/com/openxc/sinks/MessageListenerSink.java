package com.openxc.sinks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.openxc.NoValueException;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;

/**
 * A data sink that sends new measurements of specific types to listeners.
 *
 * Applications requesting asynchronous updates for specific signals get their
 * values through this sink.
 */
public class MessageListenerSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "MessageListenerSink";

    // The non-persistent listeners will be removed after they receive their
    // first message.
    private Map<KeyMatcher, MessageListenerGroup>
            mMessageListeners = new HashMap<>();
    private Multimap<Class<? extends Measurement>, Measurement.Listener>
            mMeasurementTypeListeners = HashMultimap.create();
    private Multimap<Class<? extends VehicleMessage>, VehicleMessage.Listener>
            mMessageTypeListeners = HashMultimap.create();

    private class MessageListenerGroup {
        
        ArrayList<VehicleMessage.Listener> mPersistentListeners = new ArrayList<>();
        ArrayList<VehicleMessage.Listener> mListeners = new ArrayList<>();
        
        void add(VehicleMessage.Listener listener, boolean persist) {
            if (persist) {
                mPersistentListeners.add(listener);
            } else {
                mListeners.add(listener);
            }
        }
        
        void removePersistent(VehicleMessage.Listener listener) {
            mPersistentListeners.remove(listener);
        }
        
        void receive(VehicleMessage message) {
            for (VehicleMessage.Listener listener : mPersistentListeners) {
                listener.receive(message);
            }
            for (VehicleMessage.Listener listener : mListeners) {
                listener.receive(message);
            }   
            mListeners = new ArrayList<>(); //delete all non-persistent
        }
        
        boolean isEmpty() {
            return mPersistentListeners.isEmpty() && mListeners.isEmpty();
        }   
    }
    
    public MessageListenerSink() {
        super();
    }

    public synchronized void register(KeyMatcher matcher,
            VehicleMessage.Listener listener, boolean persist) {
       
        MessageListenerGroup group = mMessageListeners.get(matcher);
        if (group == null) {
            group = new MessageListenerGroup();
            mMessageListeners.put(matcher, group);
        }
        
        group.add(listener, persist);
    }

    public synchronized void register(KeyMatcher matcher,
            VehicleMessage.Listener listener) {
        register(matcher, listener, true);
    }

    public synchronized void register(
            Class<? extends VehicleMessage> messageType,
            VehicleMessage.Listener listener) {
        mMessageTypeListeners.put(messageType, listener);
    }

    public void register(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        try {
            // A bit of a hack to cache this measurement's ID field so we
            // can deserialize incoming measurements of this type. Why don't we
            // have a getId() in the Measurement interface? Ah, because it would
            // have to be static and you can't have static methods in an
            // interface. It would work if we were passed an instance of the
            // measurement in this function, but we don't really have that when
            // adding a listener.
            BaseMeasurement.getKeyForMeasurement(measurementType);
        } catch(UnrecognizedMeasurementTypeException e) { }

        mMeasurementTypeListeners.put(measurementType, listener);
    }

    public synchronized void unregister(
            Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        mMeasurementTypeListeners.remove(measurementType, listener);
    }

    public synchronized void unregister(
            Class<? extends VehicleMessage> messageType,
            VehicleMessage.Listener listener) {
        mMessageTypeListeners.remove(messageType, listener);
    }

    public synchronized void unregister(KeyMatcher matcher,
            VehicleMessage.Listener listener) {
       
        MessageListenerGroup group = mMessageListeners.get(matcher);
        if (group != null) {
            group.removePersistent(listener);
            pruneListeners(matcher);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("numMessageListeners", mMessageListeners.size())
            .add("numMessageTypeListeners", mMessageTypeListeners.size())
            .add("numPersistentMessageListeners", getNumPersistentListeners())
            .add("numMeasurementTypeListeners", mMeasurementTypeListeners.size())
            .toString();
    }
    
    private int getNumPersistentListeners() {
        int sum = 0;
        for (KeyMatcher matcher : mMessageListeners.keySet()) {
            sum += mMessageListeners.get(matcher).mPersistentListeners.size();
        }
        
        return sum;
    }
    
    private void pruneListeners(KeyMatcher matcher) {
        MessageListenerGroup group = mMessageListeners.get(matcher);
        if (group != null && group.isEmpty()) {
            mMessageListeners.remove(matcher);
        }
    }

    @Override
    protected synchronized void propagateMessage(VehicleMessage message) {
        if (message instanceof KeyedMessage) {

            Set<KeyMatcher> matchedKeys = new HashSet<>();
            for (KeyMatcher matcher : mMessageListeners.keySet()) {
                if (matcher.matches(message.asKeyedMessage())) {
                    MessageListenerGroup group = mMessageListeners.get(matcher);
                    group.receive(message);
                    matchedKeys.add(matcher);
                }
            }

            for (KeyMatcher matcher : matchedKeys) {
                pruneListeners(matcher);
            }
            
            if (message instanceof SimpleVehicleMessage) {
                propagateMeasurementFromMessage(message.asSimpleMessage());
            }
        }

        if(mMessageTypeListeners.containsKey(message.getClass())) {
            for(VehicleMessage.Listener listener :
                    mMessageTypeListeners.get(message.getClass())) {
                listener.receive(message);
            }
        }
    }

    private synchronized void propagateMeasurementFromMessage(
            SimpleVehicleMessage message) {
        try {
            Measurement measurement =
                BaseMeasurement.getMeasurementFromMessage(message);
            if(mMeasurementTypeListeners.containsKey(measurement.getClass())) {
                for(Measurement.Listener listener :
                        mMeasurementTypeListeners.get(measurement.getClass())) {
                    listener.receive(measurement);
                }
            }
        } catch(UnrecognizedMeasurementTypeException e) {
            // The message is not a recognized Measurement, we don't propagate
            // it as a Measurement (only as a Message, which is handled
            // earlier).
        } catch(NoValueException e) {
            Log.w(TAG, "Received notification for a blank measurement", e);
        }
    }
}
