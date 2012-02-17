package com.openxc.measurements;

import com.openxc.units.Percentage;
import com.openxc.util.Range;

/**
 * The FuelLevel is the current level of fuel in the gas tank.
 */
public class FuelLevel extends Measurement<Percentage>
        implements VehicleMeasurement {
    private final static Range<Percentage> RANGE =
        new Range<Percentage>(new Percentage(0), new Percentage(100));
    public final static String ID = "fuel_level";

    public FuelLevel(Double value) {
        super(new Percentage(value), RANGE);
    }

    public FuelLevel(Percentage value) {
        super(value, RANGE);
    }
}
