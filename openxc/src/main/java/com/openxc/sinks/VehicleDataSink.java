package com.openxc.sinks;

import com.openxc.remote.RawMeasurement;

import com.openxc.VehicleDataEndpoint;

/**
 * The interface for all vehicle data destination endpoints.
 *
 * A VehicleDataSink in a {@link com.openxc.DataPipeline} is given new
 * measurements via the receive methods. Data sinks are registered with a
 * {@link com.openxc.remote.VehicleService} receive all raw messages
 * from the vehicle data sources as they arrive without having to explicitly
 * register for asynchronous updates on specific measurements. Common
 * applications of this class are trace file recording, web streaming or custom
 * CAN message handling.
 */
public interface VehicleDataSink extends VehicleDataEndpoint {
    /**
     * Receive a data point with a name, a value and a event value.
     *
     * The implementation of this method should not block, lest the vehicle data
     * source get behind in processing data from a source potentially external
     * to the system.
     *
     * @param measurement The new measurement.
     */
    public void receive(RawMeasurement measurement) throws DataSinkException;
}
