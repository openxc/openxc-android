package com.openxc.sinks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.openxc.remote.RawMeasurement;

/**
 * A common parent class for all vehicle data sinks.
 *
 * Many sinks require a reference to last known value of all measurements. This
 * class encapsulates the functionality require to store a reference to the
 * measurements data structure and query it for values.
 */
public class BaseVehicleDataSink implements VehicleDataSink {
    private Map<String, RawMeasurement> mMeasurements =
            new ConcurrentHashMap<String, RawMeasurement>();

    /**
     * Receive a raw measurement, deserialized to primatives.
     *
     * Children of this class can call super.receive() if they need to store
     * copies of received measurements to access via the get(String) method.
     */
    public boolean receive(RawMeasurement measurement) throws DataSinkException {
        mMeasurements.put(measurement.getName(), measurement);
        return true;
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
