package com.openxc.sources;

import com.openxc.sources.SourceCallback;

import com.openxc.VehicleDataEndpoint;

/**
 * The interface for all sources of raw vehicle measurements.
 *
 * Data is retrieved from a vehicle source by registering a callback object that
 * implements the DataPipeline - its receive() methods are
 * passed values from the data source.
 */
public interface VehicleDataSource extends VehicleDataEndpoint {
    /**
     * Set the callback for receiving raw measurements as they are received.
     *
     * Vehicle data sources only need to support a single callback, and in fact
     * should not support more than one - all vehicle measurements should be
     * directed to a single, central collector.
     */
    public void setCallback(SourceCallback callback);
}
