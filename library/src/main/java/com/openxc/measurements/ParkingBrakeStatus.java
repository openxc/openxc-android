package com.openxc.measurements;

/**
 * The ParkingBrakeStatus measurement knows if the parking brake is engaged or not.
 */
public class ParkingBrakeStatus extends BaseMeasurement<com.openxc.units.Boolean> {
    public final static String ID = "parking_brake_status";

    public ParkingBrakeStatus(com.openxc.units.Boolean value) {
        super(value);
    }

    public ParkingBrakeStatus(java.lang.Boolean value) {
        this(new com.openxc.units.Boolean(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
