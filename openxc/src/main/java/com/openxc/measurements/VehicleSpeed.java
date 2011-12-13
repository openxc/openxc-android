package com.openxc.measurements;

import com.openxc.units.MetersPerSecond;
import com.openxc.util.Range;

/**
 * The VehicleSpeed is the current forward speed of the vehicle.
 */
public class VehicleSpeed extends Measurement<MetersPerSecond>
        implements VehicleMeasurement {
    // TODO this should change based on the vehicle platform - needs to be read
    // from the CAN translator (which has this info stored statically)
    private final static Range<MetersPerSecond> RANGE =
        new Range<MetersPerSecond>(new MetersPerSecond(0.0),
                new MetersPerSecond(60.0));
    public final static String ID = "vehicle_speed";

    public VehicleSpeed() { }

    public VehicleSpeed(MetersPerSecond value) {
        super(value, RANGE);
    }

    public VehicleSpeed(Double value) {
        this(new MetersPerSecond(value));
    }
}
