package com.openxc.measurements;

import com.openxc.units.GearPosition;

/* This may seem like a "psuedo-typedef" class but we're using it it to enforce
 * the binding between the measurement and its unit type. This unfortunately
 * means we have to add constructors because they aren't inherited from
 * Measurement. If you know of a better way, please speak up.
 */
public class TransmissionGearPosition extends Measurement<GearPosition>
        implements VehicleMeasurement {
    private final static String VEHICLE_SPEED_ID = "transmission_gear";

    public TransmissionGearPosition(GearPosition value) {
        super(value);
    }

    public String getId() {
        return VEHICLE_SPEED_ID;
    }
}
