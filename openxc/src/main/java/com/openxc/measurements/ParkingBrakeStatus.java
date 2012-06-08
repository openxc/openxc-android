package com.openxc.measurements;

import com.openxc.units.Boolean;

/**
 * The ParkingBrakeStatus measurement knows if the parking brake is engaged or not.
 */
public class ParkingBrakeStatus extends BaseMeasurement<Boolean> {
    public final static String ID = "parking_brake_status";

    public ParkingBrakeStatus(Boolean value) {
        super(value);
    }

    public ParkingBrakeStatus(boolean value) {
        this(new Boolean(value));
    }

    public ParkingBrakeStatus(Double value) {
        this(new Boolean(value));
    }
}
