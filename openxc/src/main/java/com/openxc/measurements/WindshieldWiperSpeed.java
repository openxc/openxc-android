package com.openxc.measurements;

import com.openxc.units.Level;

import com.openxc.util.Range;

public class WindshieldWiperSpeed extends Measurement<Level>
        implements VehicleMeasurement {
    public final static String ID = "windshield_wiper_speed";
    private final static Range<Level> RANGE =
        new Range<Level>(new Level(0), new Level(31));

    public WindshieldWiperSpeed() { }

    public WindshieldWiperSpeed(Double value) {
        super(new Level(value), RANGE);
    }

    public WindshieldWiperSpeed(double value) {
        this(new Double(value));
    }
}
