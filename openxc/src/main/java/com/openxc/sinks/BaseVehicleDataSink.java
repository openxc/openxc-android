package com.openxc.sinks;

import com.openxc.remote.RawMeasurement;
import java.util.Map;

/**
 * A common parent class for all vehicle data sinks.
 *
 * Many sinks require a reference to last known value of all measurements. This
 * class encapsulates the functionality require to store a reference to the
 * measurements data structure and query it for values.
 */
public class BaseVehicleDataSink implements VehicleDataSink {
    protected Map<String, RawMeasurement> mMeasurements;

    public BaseVehicleDataSink() { }

    public BaseVehicleDataSink(Map<String, RawMeasurement> measurements) {
        mMeasurements = measurements;
    }

    /**
     * Receive a data point with a name, a value and a event value.
     *
     * @see VehicleDataSink.receive(measurementId, Object, Object)
     *
     * If you override this method be sure to call super.receive to make sure
     * a RawMeasurement is created and passed to
     * {@link #receive(String, RawMeasurement)}, unless you don't want that to
     * happen.
     */
    public void receive(String measurementId, Object value, Object event) {
        RawMeasurement measurement =
            RawMeasurement.measurementFromObjects(value, event);
        receive(measurementId, measurement);
    }

    /**
     * Receive a data point with a name and value.
     *
     * This method is the same as {@link #receive(String, Object, Object)} but
     * passes null for the optional event.
     *
     * @param name The name of the element.
     * @param value The String value of the element.
     */
    public void receive(String measurementId, Object value) {
        receive(measurementId, value, null);
    }

    /**
     * Receive a measurement serialized to Double in RawMeasurement.
     *
     * The reason some sinks will need the raw objects vs. the RawMeasurement is
     * that once we construct the RawMeasurement, the actual object values are
     * pseduo serialized to a Double in order to pass through the AIDL
     * interface.
     */
    public void receive(String measurementId, RawMeasurement measurement) {
        // do nothing unless you override it
    }

    public void setMeasurements(Map<String, RawMeasurement> measurements) {
        mMeasurements = measurements;
    }

    public boolean containsMeasurement(String measurementId) {
        return mMeasurements.containsKey(measurementId);
    }

    public void stop() {
        // do nothing unless you need it
    }
}
