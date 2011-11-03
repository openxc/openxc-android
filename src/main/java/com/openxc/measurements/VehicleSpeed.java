package com.openxc.measurements;

import com.openxc.units.MetersPerSecond;
import com.openxc.util.Range;

/* This may seem like a "psuedo-typedef" class but we're using it it to enforce
 * the binding between the measurement and its unit type. This unfortunately
 * means we have to add constructors because they aren't inherited from
 * Measurement. If you know of a better way, please speak up.
 */
public class VehicleSpeed extends Measurement<MetersPerSecond>
        implements VehicleMeasurement {
    // TODO this should change based on the vehicle platform - needs to be read
    // from the CAN translator (which has this info stored statically)
    private final static Range<MetersPerSecond> RANGE =
        new Range<MetersPerSecond>(new MetersPerSecond(0.0),
                new MetersPerSecond(60.0));
    private final static String VEHICLE_SPEED_ID = "vehicle_speed";

    public VehicleSpeed(MetersPerSecond value) {
        super(value, RANGE);
    }

    public String getId() {
        return VEHICLE_SPEED_ID;
    }
}
