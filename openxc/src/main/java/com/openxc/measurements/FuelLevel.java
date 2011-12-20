package com.openxc.measurements;

import com.openxc.units.Liter;
import com.openxc.util.Range;

/**
 * The FuelLevel is the current level of fuel in the gas tank.
 */
public class FuelLevel extends Measurement<Liter>
        implements VehicleMeasurement {
    private final static Range<Liter> RANGE =
        new Range<Liter>(new Liter(0), new Liter(204.6));
    public final static String ID = "fuel_level";

    public FuelLevel() { }

    public FuelLevel(Double value) {
        super(new Liter(value), RANGE);
    }

    public FuelLevel(Liter value) {
        super(value, RANGE);
    }
}
