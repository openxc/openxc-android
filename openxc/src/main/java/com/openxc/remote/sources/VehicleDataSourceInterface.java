package com.openxc.remote.sources;

/**
 * The interface for all sources of raw vehicle measurements.
 *
 * Data is retrieved from a vehicle source by registering a callback object that
 * implements the VehicleDataSourceCallbackInterface - its receive() methods are
 * passed values from the data source.
 */
public interface VehicleDataSourceInterface extends Runnable {
    /**
     * Set the callback for receiving raw measurements as they are received.
     *
     * Vehicle data sources only need to support a single callback, and in fact
     * should not support more than one - all vehicle measurements should be
     * directed to a single, central collector.
     */
    public void setCallback(VehicleDataSourceCallbackInterface callback);

    /**
     * Release any acquired resources in preparation for exiting.
     */
    public void stop();
}
