package com.openxc.measurements;

import com.openxc.units.Degree;
import com.openxc.util.Range;

public class SteeringWheelAngle extends Measurement<Degree>
        implements VehicleMeasurement {
    private final static Range<Degree> RANGE =
        new Range<Degree>(new Degree(-400), new Degree(400));
    public final static String ID = "SteeringWheelAngle";

    public SteeringWheelAngle(double value) {
        super(new Degree(value), RANGE);
    }
    public SteeringWheelAngle(Degree value) {
        super(value, RANGE);
    }
}
