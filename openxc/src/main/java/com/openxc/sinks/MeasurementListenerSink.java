package com.openxc.sinks;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multimap;

import com.openxc.measurements.MeasurementInterface;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.Measurement;

import com.openxc.NoValueException;
import com.openxc.remote.RawMeasurement;

import android.util.Log;

/**
 * A data sink that sends new measurements of specific types to listeners.
 *
 * Applications requesting asynchronous updates for specific signals get their
 * values through this sink.
 */
public class MeasurementListenerSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "MeasurementListenerSink";

    private Multimap<Class<? extends MeasurementInterface>,
            MeasurementInterface.Listener> mListeners;

    public MeasurementListenerSink() {
        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
    }

    public void register(Class<? extends MeasurementInterface> measurementType,
            Measurement.Listener listener)
            throws UnrecognizedMeasurementTypeException {
        mListeners.put(measurementType, listener);

        String measurementId = Measurement.getIdForClass(measurementType);
        if(containsMeasurement(measurementId)) {
            // send the last known value to the new listener
            RawMeasurement rawMeasurement = mMeasurements.get(measurementId);
            receive(measurementId, rawMeasurement);
        }
    }

    public void unregister(Class<? extends MeasurementInterface> measurementType,
            Measurement.Listener listener) {
        mListeners.remove(measurementType, listener);
    }

    protected void propagateMeasurement(String measurementId,
            RawMeasurement rawMeasurement) {
        try {
            MeasurementInterface measurement = createMeasurement(
                    measurementId, rawMeasurement);
            for(MeasurementInterface.Listener listener :
                    mListeners.get(Measurement.getClassForId(measurementId))) {
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

    private static MeasurementInterface createMeasurement(
            String measurementId, RawMeasurement value)
            throws UnrecognizedMeasurementTypeException, NoValueException {
        Class<? extends MeasurementInterface> measurementClass =
            Measurement.getClassForId(measurementId);
        return Measurement.getMeasurementFromRaw(
                measurementClass, value);
    }
}
