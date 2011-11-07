package com.openxc.measurements;

public interface VehicleMeasurement {
    public interface Listener {
        public void receive(VehicleMeasurement measurement);
    }
}
