package com.openxc.measurements;

import com.openxc.units.Degree;
import com.openxc.util.Range;

/**
 * The Longitude is the current longitude of the vehicle in degrees according to
 * GPS.
 */
public class Longitude extends Measurement<Degree> {
    private final static Range<Degree> RANGE = new Range<Degree>(
            new Degree(-179.0), new Degree(179.0));
    public final static String ID = "longitude";

    public Longitude(Degree value) {
        super(value, RANGE);
    }

    public Longitude(Double value) {
        this(new Degree(value));
    }
}
