package com.openxc;

import com.google.common.base.Objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multimap;

import com.openxc.measurements.MeasurementInterface;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.Measurement;

import com.openxc.remote.NoValueException;
import com.openxc.remote.RawMeasurement;

import com.openxc.remote.sinks.BaseVehicleDataSink;

import android.util.Log;

public class ListenerSink extends BaseVehicleDataSink {
    private final static String TAG = "ListenerSink";

    private Multimap<Class<? extends MeasurementInterface>,
            MeasurementInterface.Listener> mListeners;
    private BiMap<String, Class<? extends MeasurementInterface>>
            mMeasurementIdToClass;

    public ListenerSink(BiMap<String, Class<? extends MeasurementInterface>>
                measurementIdToClass) {
        mMeasurementIdToClass = measurementIdToClass;
        mListeners = HashMultimap.create();
        mListeners = Multimaps.synchronizedMultimap(mListeners);
    }

    public void register(Class<? extends MeasurementInterface> measurementType,
            Measurement.Listener listener) {
        mListeners.put(measurementType, listener);

        String measurementId = mMeasurementIdToClass.inverse().get(measurementType);
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

    // TODO we may want to dump these in a queue handled by another
    // thread or post runnables to the main handler, sort of like we
    // do in the RemoteVehicleService. If the listener's receive
    // blocks...actually it might be OK.
    //
    // we do this in RVS because the data source would block waiting
    // for the receive to return before handling another.
    //
    // in this case we're being called from the handler thread in
    // RVS...so yeah, we don't want to block.
    //
    // AppLink posts runnables, but that might create a ton of
    // objects and be a lot of overhead. the queue method might be
    // fine, and if we use that we should see if the queue+notifying
    // thread setup can be abstracted and shared by the two
    // services.
    public void receive(String measurementId, RawMeasurement rawMeasurement) {
        synchronized(mListeners) {
            MeasurementInterface measurement = createMeasurement(
                    measurementId, rawMeasurement);
            for(MeasurementInterface.Listener listener :
                    mListeners.get(mMeasurementIdToClass.get(measurementId))) {
                listener.receive(measurement);
            }
        }
    }

    private MeasurementInterface createMeasurement(
            String measurementId, RawMeasurement value) {
        Class<? extends MeasurementInterface> measurementClass =
            mMeasurementIdToClass.get(measurementId);
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("numListeners", mListeners.size())
            .toString();
    }
}
