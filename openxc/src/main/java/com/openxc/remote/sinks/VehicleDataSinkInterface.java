package com.openxc.remote.sinks;

import com.openxc.remote.RawMeasurement;

/**
 * The interface for all output targets for raw vehicle measurements.
 *
 * Data sinks are registered with the
 * {@link com.openxc.remote.RemoteVehicleService} and receive all raw messages
 * from the vehicle data source as they arrive. Common applications of this
 * class are trace file recording, web streaming or custom CAN message handling.
 */
public interface VehicleDataSinkInterface {
    /**
     * Do something with a new, raw vehicle message.
     *
     * The implementation of this method must not block, as it is called in the
     * same thread that processes incoming messages in RemoteVehicleService.
     */
    public void receive(String measurementId, Object value, Object event);

    /**
     * Release any acquired resources in preparation for exiting.
     */
    public void stop();
}
