package com.openxc.measurements;

import com.openxc.units.Degree;
import com.openxc.util.Range;

/**
 * The Latitude is the current latitude of the vehicle in degrees according to
 * GPS.
 */
public class Latitude extends BaseMeasurement<Degree> {
    private final static Range<Degree> RANGE = new Range<>(
            new Degree(-89.0), new Degree(89.0));
    public final static String ID = "latitude";

    public Latitude(Degree value) {
        super(value, RANGE);
    }

    public Latitude(Number value) {
        this(new Degree(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
