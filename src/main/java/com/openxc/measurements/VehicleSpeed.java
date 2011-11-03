package com.openxc.measurements;

import com.openxc.units.MetersPerSecond;
import com.openxc.util.Range;

public class VehicleSpeed extends Measurement<MetersPerSecond> {
    // TODO this should change based on the vehicle platform - needs to be read
    // from the CAN translator (which has this info stored statically)
    private final static Range<MetersPerSecond> RANGE =
        new Range<MetersPerSecond>(new MetersPerSecond(0.0),
                new MetersPerSecond(60.0));

    public VehicleSpeed(MetersPerSecond value) {
        super(value, RANGE);
    }
}
