package com.openxc.sinks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multimap;

import com.openxc.measurements.MeasurementInterface;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.Measurement;

import com.openxc.NoValueException;
import com.openxc.remote.RawMeasurement;

import android.util.Log;

public class MeasurementListenerSink extends AbstractQueuedCallbackSink {
    private final static String TAG = "MeasurementListenerSink";

    private Multimap<Class<? extends MeasurementInterface>,
            MeasurementInterface.Listener> mListeners;
    private static BiMap<String, Class<? extends MeasurementInterface>>
            sMeasurementIdToClass;

    public MeasurementListenerSink(BiMap<String, Class<? extends MeasurementInterface>>
                measurementIdToClass) {
        sMeasurementIdToClass = measurementIdToClass;
        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
    }

    public void register(Class<? extends MeasurementInterface> measurementType,
            Measurement.Listener listener) {
        mListeners.put(measurementType, listener);

        String measurementId = sMeasurementIdToClass.inverse().get(measurementType);
        if(containsMeasurement(measurementId)) {
            // send the last known value to the new listener
            RawMeasurement rawMeasurement = get(measurementId);
            receive(measurementId, rawMeasurement);
        }
    }

    public void unregister(Class<? extends MeasurementInterface> measurementType,
            Measurement.Listener listener) {
        mListeners.remove(measurementType, listener);
    }

    protected void propagateMeasurement(
            String measurementId,
            RawMeasurement rawMeasurement) {
        MeasurementInterface measurement = createMeasurement(
                measurementId, rawMeasurement);
        for(MeasurementInterface.Listener listener :
                mListeners.get(sMeasurementIdToClass.get(measurementId))) {
            listener.receive(measurement);
        }
    }

    private static MeasurementInterface createMeasurement(
            String measurementId, RawMeasurement value) {
        Class<? extends MeasurementInterface> measurementClass =
            sMeasurementIdToClass.get(measurementId);
        MeasurementInterface measurement = null;
        try {
            measurement = Measurement.getMeasurementFromRaw(
                    measurementClass, value);
        } catch(UnrecognizedMeasurementTypeException e) {
            Log.w(TAG, "Received notification for a malformed " +
                    "measurement type: " + measurementClass, e);
        } catch(NoValueException e) {
            Log.w(TAG, "Received notification for a blank " +
                    "measurement of type: " + measurementClass, e);
        }
        return measurement;
    }

    public Multimap<Class<? extends MeasurementInterface>,
           Measurement.Listener> getListeners() {
        return mListeners;
    }
}
