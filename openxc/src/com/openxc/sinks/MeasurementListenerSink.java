package com.openxc.sinks;

import android.util.Log;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.openxc.NoValueException;
import com.openxc.measurements.BaseMeasurement;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.RawMeasurement;

/**
 * A data sink that sends new measurements of specific types to listeners.
 *
 * Applications requesting asynchronous updates for specific signals get their
 * values through this sink.
 */
public class MeasurementListenerSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "MeasurementListenerSink";

    private Multimap<Class<? extends Measurement>,
            Measurement.Listener> mListeners = HashMultimap.create();

    public MeasurementListenerSink() {
        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
    }

    public void register(Class<? extends Measurement> measurementType,
            Measurement.Listener listener)
            throws UnrecognizedMeasurementTypeException {
        mListeners.put(measurementType, listener);

        String measurementId = BaseMeasurement.getIdForClass(measurementType);
        if(containsMeasurement(measurementId)) {
            // send the last known value to the new listener
            try {
                receive(get(measurementId));
            } catch(DataSinkException e) {
                Log.w(TAG, "Sink could't receive measurement", e);
            }
        }
    }

    public void unregister(Class<? extends Measurement> measurementType,
            Measurement.Listener listener) {
        mListeners.remove(measurementType, listener);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("numListeners", mListeners.size())
            .toString();
    }

    protected void propagateMeasurement(String measurementId,
            RawMeasurement rawMeasurement) {
        try {
            Measurement measurement = BaseMeasurement.getMeasurementFromRaw(
                    rawMeasurement);
            for(Measurement.Listener listener :
                    mListeners.get(BaseMeasurement.getClassForId(measurementId))) {
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
