package com.openxc.measurements;

/**
 * An interface for measurements that wish to receive callbacks for measurement
 * updates.
 *
 * TODO I can't clearly explain the need for this class - can it be merged in
 * with another one?
 */
public interface VehicleMeasurement extends MeasurementInterface {
    public interface Listener {
        public void receive(VehicleMeasurement measurement);
    }
}
