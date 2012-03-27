package com.openxc.remote.sinks;

import com.openxc.remote.RawMeasurement;

/**
 * The interface for all output of raw vehicle measurements.
 */
public interface VehicleDataSinkInterface {
    /**
     * Set the callback for receiving measurements as they are received.
     *
     * Vehicle data sources only need to support a single callback.
     */
    public void receive(String measurementId, Object value, Object event);

    /**
     * Release any acquired resources in preparation for exiting.
     */
    public void stop();
}
