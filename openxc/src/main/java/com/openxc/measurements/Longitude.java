package com.openxc.measurements;

import com.openxc.units.Degree;
import com.openxc.util.Range;

public class Longitude extends Measurement<Degree>
        implements VehicleMeasurement {
    private final static Range<Degree> RANGE = new Range<Degree>(
            new Degree(-179.0), new Degree(179.0));
    public final static String ID = "longitude";

    public Longitude() { }

    public Longitude(Degree value) {
        super(value, RANGE);
    }

    public Longitude(Double value) {
        this(new Degree(value));
    }
}
