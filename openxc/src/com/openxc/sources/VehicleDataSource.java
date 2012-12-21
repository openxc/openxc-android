package com.openxc.sources;

import com.openxc.sources.SourceCallback;

/**
 * The interface for all sources of raw vehicle measurements.
 *
 * Data is retrieved from a vehicle source by registering a callback object that
 * implements the DataPipeline - its receive() methods are
 * passed values from the data source.
 */
public interface VehicleDataSource {
    /**
     * Set the callback for receiving raw measurements as they are received.
     *
     * Vehicle data sources only need to support a single callback, and in fact
     * should not support more than one - all vehicle measurements should be
     * directed to a single, central collector.
     */
    public void setCallback(SourceCallback callback);

    /**
     * Release any acquired resources and either stop sending measurements (if a
     * source) or stop expecting to receive them (if a sink).
     */
    public void stop();
}
