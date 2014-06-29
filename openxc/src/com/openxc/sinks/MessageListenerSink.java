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
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
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
    private Multimap<KeyMatcher, DiagnosticResponse.Listener>
            mDiagnosticListeners = HashMultimap.create();
    private Map<MessageKey, DiagnosticRequest> mRequestMap = new HashMap<>();

    public MessageListenerSink() {
        mMessageListeners = Multimaps.synchronizedMultimap(mMessageListeners);
        mMeasurementListeners = Multimaps.synchronizedMultimap(
                mMeasurementListeners);
        mDiagnosticListeners = Multimaps.synchronizedMultimap(
                mDiagnosticListeners);
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

    public void register(KeyMatcher matcher,
            DiagnosticResponse.Listener listener) {
        mDiagnosticListeners.put(matcher, listener);
    }

    public void record(DiagnosticRequest request) {
        // Sending a request with the same key as a previous
        // one cancels the last one, so just overwrite it
        mRequestMap.put(request.getKey(), request);
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

    public void unregister(KeyMatcher matcher, DiagnosticResponse.Listener listener) {
        mMeasurementListeners.remove(matcher, listener);
    }

    public void unregister(DiagnosticResponse.Listener listener) {
        for (KeyMatcher matcher : mDiagnosticListeners.keys()) {
            Collection<DiagnosticResponse.Listener> listeners = mDiagnosticListeners.get(matcher);
            if (listeners.contains(listener)) {
                listeners.remove(listener);
            }
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
        } else if (message instanceof DiagnosticResponse) {
            propagateDiagnosticResponse((DiagnosticResponse) message);
        }
    }

    private void propagateDiagnosticResponse(DiagnosticResponse response) {
        DiagnosticRequest request = mRequestMap.get(response.getKey());
        for (KeyMatcher matcher : mDiagnosticListeners.keys()) {
            if (matcher.matches(response)) {
                for (DiagnosticResponse.Listener listener : mDiagnosticListeners.get(matcher)) {
                    listener.receive(request, response);
                }
            }
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
