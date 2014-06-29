package com.openxc.sinks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.openxc.NoValueException;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.ExactKeyMatcher;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.MessageKey;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.VehicleMessage;

/**
 * A data sink that sends new measurements of specific types to listeners.
 *
 * Applications requesting asynchronous updates for specific signals get their
 * values through this sink.
 */
public class MessageListenerSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "MessageListenerSink";

    private Multimap<KeyMatcher, VehicleMessage.Listener>
            mMessageListeners = HashMultimap.create();
    private Multimap<KeyMatcher, Measurement.Listener>
            mMeasurementListeners = HashMultimap.create();

    public MessageListenerSink() {
        mMessageListeners = Multimaps.synchronizedMultimap(mMessageListeners);
        mMeasurementListeners = Multimaps.synchronizedMultimap(
                mMeasurementListeners);
    }

    public void register(KeyMatcher matcher, VehicleMessage.Listener listener) {
        mMessageListeners.put(matcher, listener);
    }

    public void register(KeyedMessage message,
            VehicleMessage.Listener listener) {
        register(ExactKeyMatcher.buildExactMatcher(message), listener);
    }

    public void register(KeyMatcher matcher, Measurement.Listener listener) {
        // TODO how do we handle listeners for measurements, not messages?
    }

    public void register(KeyedMessage message,
            VehicleMessage.Listener listener) {
        register(ExactKeyMatcher.buildExactMatcher(message), listener);
    }

    public void unregister(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        // TODO hack alert! need to refactor this
        try {
        mMeasurementListeners.remove(ExactKeyMatcher.buildExactMatcher(
                    new NamedVehicleMessage(
                        BaseMeasurement.getIdForClass(measurementType))),
                listener);
        } catch(UnrecognizedMeasurementTypeException e) {
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("numListeners", mMeasurementListeners.size())
            .toString();
    }

    protected void propagateMessage(VehicleMessage message) {
        if(message instanceof KeyedMessage) {
            for (KeyMatcher matcher : mMessageListeners.keys()) {
                if (matcher.matches(message.asKeyedMessage())) {
                    for (VehicleMessage.Listener listener :
                            mMessageListeners.get(matcher)) {
                        listener.receive(message);
                    }
                }
            }
        }

        if (message instanceof SimpleVehicleMessage) {
            propagateMeasurementFromMessage(message.asSimpleMessage());
        }
    }

    private void propagateMeasurementFromMessage(SimpleVehicleMessage message) {
        try {
            Measurement measurement =
                BaseMeasurement.getMeasurementFromMessage(message);
            for (KeyMatcher matcher : mMeasurementListeners.keys()) {
                if (matcher.matches(message)) {
                    for (Measurement.Listener listener : mMeasurementListeners.get(matcher)) {
                        listener.receive(measurement);
                    }
                }
            }
        } catch(UnrecognizedMeasurementTypeException e) {
            // This happens quite often if nobody has registered to receive
            // updates for the specific signal. It can be an error, but if we
            // log here it's really, really noisy.
        } catch(NoValueException e) {
            Log.w(TAG, "Received notification for a blank measurement", e);
        }
    }
}
