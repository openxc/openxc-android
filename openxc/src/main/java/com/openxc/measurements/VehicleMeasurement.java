package com.openxc.measurements;

public interface VehicleMeasurement extends MeasurementInterface {
    public interface Listener {
        public void receive(VehicleMeasurement measurement);
    }
}
