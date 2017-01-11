package com.openxc.sources;

import com.openxc.DataPipeline;

/**
 * The interface for all sources of raw vehicle measurements.
 *
 * Data is retrieved from a vehicle source by registering a callback object that
 * implements the DataPipeline - its receive() methods are
 * passed values from the data source.
 */
public interface VehicleDataSource extends DataPipeline.Operator {
    /**
     * Set the callback for receiving raw measurements as they are received.
     *
     * Vehicle data sources only need to support a single callback, and in fact
     * should not support more than one - all vehicle measurements should be
     * directed to a single, central collector.
     */
    public void setCallback(SourceCallback callback);

    /**
     * Return true if the data source is actively connected to its target, be it
     * a USB endpoint, a Bluetooth channel, a trace file, etc. The source is
     * capable of providing new vehicle data.
     *
     * Returns true if connected, false otherwise.
     */
    public boolean isConnected();

    /**
     * Release any acquired resources and either stop sending measurements (if a
     * source) or stop expecting to receive them (if a sink).
     */
    public void stop();
}
