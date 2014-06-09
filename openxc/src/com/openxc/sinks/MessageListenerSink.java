package com.openxc.sinks;

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
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
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

    private Multimap<Class<? extends Measurement>, Measurement.Listener>
            mMeasurementListeners = HashMultimap.create();
    private Multimap<String, DiagnosticResponse.Listener>
            mDiagnosticListeners = HashMultimap.create();
    private Map<String, DiagnosticRequest> mRequestMap = new HashMap<>();

    public MessageListenerSink() {
        mMeasurementListeners = Multimaps.synchronizedMultimap(
                mMeasurementListeners);
        mDiagnosticListeners = Multimaps.synchronizedMultimap(
                mDiagnosticListeners);
    }

    public void register(Class<? extends Measurement> measurementType,
            Measurement.Listener listener)
            throws UnrecognizedMeasurementTypeException {
        mMeasurementListeners.put(measurementType, listener);

        String name = BaseMeasurement.getIdForClass(measurementType);
        if(containsNamedMessage(name)) {
            // send the last known value to the new listener
            try {
                receive(getNamedMessage(name));
            } catch(DataSinkException e) {
                Log.w(TAG, "Sink could't receive measurement", e);
            }
        }
    }

    public void registerUntilDone(DiagnosticRequest request,
            DiagnosticResponse.Listener listener) {
        String diagnosticIdentifier = request.getDiagnosticIdentifier();
        // Sending a request with the same diagnosticIdentifier as a previous
        // one cancels the last one, so just overwrite it
        mRequestMap.put(diagnosticIdentifier, request);
        mDiagnosticListeners.put(diagnosticIdentifier, listener);
    }

    public void unregister(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        mMeasurementListeners.remove(measurementType, listener);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("numListeners", mMeasurementListeners.size())
            .toString();
    }

    protected void propagateMessage(VehicleMessage message) {
        if(message instanceof NamedVehicleMessage) {
            propagateMeasurement((NamedVehicleMessage) message);
        } else if(message instanceof DiagnosticResponse) {
            propagateDiagnosticResponse((DiagnosticResponse) message);
        } else {
            // TODO propagate generic VehicleMessage - need a new listener
            // callback
        }
    }

    private void propagateDiagnosticResponse(DiagnosticResponse response) {
        String diagnosticIdentifier = response.getDiagnosticIdentifier();
        DiagnosticRequest request = mRequestMap.get(diagnosticIdentifier);
        for(DiagnosticResponse.Listener listener : mDiagnosticListeners.get(diagnosticIdentifier)) {
            listener.receive(request, response);
        }
        //if the frequency of the most recent request with this id is 0, it's done once the response has been received,
        //so no need to store them anymore
        if (request.getFrequency() == 0) {
            mRequestMap.remove(diagnosticIdentifier);
            mDiagnosticListeners.removeAll(diagnosticIdentifier);
        }
    }

    private void propagateMeasurement(NamedVehicleMessage message) {
        try {
            Measurement measurement =
                BaseMeasurement.getMeasurementFromMessage(message);
            for(Measurement.Listener listener :
                    mMeasurementListeners.get(measurement.getClass())) {
                listener.receive(measurement);
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
