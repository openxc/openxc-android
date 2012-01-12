package com.openxc.measurements;

import com.openxc.units.Kilometer;
import com.openxc.util.Range;

/**
 * The FineOdometer is a persistent odometer recording.
 */
public class FineOdometer extends Measurement<Kilometer>
        implements VehicleMeasurement {
    private final static Range<Kilometer> RANGE =
        new Range<Kilometer>(new Kilometer(0), new Kilometer(100));
    public final static String ID = "fine_odometer_since_restart";

    public FineOdometer() { }

    public FineOdometer(Double value) {
        super(new Kilometer(value), RANGE);
    }

    public FineOdometer(Kilometer value) {
        super(value, RANGE);
    }
}
