package com.openxc.sinks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

import com.openxc.remote.RawMeasurement;

/**
 * A common parent class for all vehicle data sinks.
 *
 * Many sinks require a reference to last known value of all measurements. This
 * class encapsulates the functionality require to store a reference to the
 * measurements data structure and query it for values.
 */
public class BaseVehicleDataSink implements VehicleDataSink {
    private Map<String, RawMeasurement> mMeasurements;

    public BaseVehicleDataSink() {
        mMeasurements = new ConcurrentHashMap<String, RawMeasurement>();
    }

    /**
     * Receive a measurement serialized to Double in RawMeasurement.
     *
     * The reason some sinks will need the raw objects vs. the RawMeasurement is
     * that once we construct the RawMeasurement, the actual object values are
     * pseduo serialized to a Double in order to pass through the AIDL
     * interface.
     */
    public void receive(RawMeasurement measurement) throws DataSinkException {
        mMeasurements.put(measurement.getName(), measurement);
    }

    public boolean containsMeasurement(String measurementId) {
        return mMeasurements.containsKey(measurementId);
    }

    public RawMeasurement get(String measurementId) {
        return mMeasurements.get(measurementId);
    }

    public Set<Map.Entry<String, RawMeasurement>> getMeasurements() {
        return mMeasurements.entrySet();
    }

    public void stop() {
        // do nothing unless you need it
    }
}
