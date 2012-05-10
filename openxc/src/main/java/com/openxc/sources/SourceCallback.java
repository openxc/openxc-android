package com.openxc.sources;

/**
 * A receipient of measurement updates from a vehicle data source.
 *
 * A VehicleDataSource is given a callback that implements this interface. When
 * new measurements arrive from the source, it uses the
 * {@link #receive(String, Object, Object)} method to pass along the new value.
 */
public interface SourceCallback {
    /**
     * Receive a new measurement with at least a value and optionally an event.
     *
     * @param measurementId the ID of the new measurement
     * @param value the value of the measurement - will always be not null.
     * @param event an optional event associated with the measurement - may be
     *              null
     */
    public void receive(String measurementId, Object value, Object event);
}
