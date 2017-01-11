package com.openxc.sources;

import com.openxc.messages.VehicleMessage;

/**
 * A recipient of measurement updates from a vehicle data source.
 *
 * A VehicleDataSource is given a callback that implements this interface. When
 * new measurements arrive from the source, it uses the
 * {@link #receive(VehicleMessage)} method to pass along the new value.
 */
public interface SourceCallback {
    /**
     * Receive a new measurement with at least a value and optionally an event.
     *
     * @param measurement the new measurement.
     */
    public void receive(VehicleMessage measurement);

    /**
     * The data source is connected, so if necessary, keep the device awake.
     */
    public void sourceConnected(VehicleDataSource source);

    /**
     * The data source is connected, so if necessary, let the device go to
     * sleep.
     */
    public void sourceDisconnected(VehicleDataSource source);
}
