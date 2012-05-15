package com.openxc.sinks;

import java.util.Map;

import com.openxc.remote.RawMeasurement;
import com.openxc.VehicleDataEndpoint;

/**
 * The interface for all vehicle data destination endpoints.
 *
 * A VehicleDataSink in a {@link DataPipeline} is given new measurements via the
 * receive methods. Data sinks are registered with a
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
     * @param name The name of the element.
     * @param value The String value of the element.
     * @param event The String event of the element.
     */
    public void receive(String measurementId, Object value, Object event);

    /**
     * Store a reference to a map of current values for all known measurements.
     *
     * This function doesn't neccessarily need to do anything unless the sink
     * demands it, but it needs to at least be defined as empty since
     * DataPipeline calls it.
     */
    public void setMeasurements(Map<String, RawMeasurement> measurements);
}
