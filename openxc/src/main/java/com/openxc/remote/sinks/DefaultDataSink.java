package com.openxc.remote.sinks;

import java.util.Map;

import com.openxc.remote.RawMeasurement;

/**
 * A callback receiver for the vehicle data source.
 *
 * The selected vehicle data source is initialized with this callback object
 * and calls its receive() methods with new values as they come in - it's
 * important that receive() not block in order to get out of the way of new
 * meausrements coming in on a physical vehcile interface.
 */
public class DefaultDataSink extends BaseVehicleDataSink {
    private final static String TAG = "DefaultDataSink";

    public void receive(String measurementId, RawMeasurement measurement) {
        if(mMeasurements != null) {
            mMeasurements.put(measurementId, measurement);
        }
    }
}
