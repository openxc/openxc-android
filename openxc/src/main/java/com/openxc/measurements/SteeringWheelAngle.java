package com.openxc.measurements;

import com.openxc.units.Degree;
import com.openxc.util.Range;

/**
 * The SteeringWheelAngle is the angle of the steering wheel.
 *
 * The wheel is considered centered with the angle is 0. The wheel is turned to
 * the left when the angle is negative, and to the right when it is positive.
 */
public class SteeringWheelAngle extends BaseMeasurement<Degree> {
    private final static Range<Degree> RANGE =
        new Range<Degree>(new Degree(-600), new Degree(600));
    public final static String ID = "steering_wheel_angle";

    public SteeringWheelAngle(Number value) {
        super(new Degree(value), RANGE);
    }

    public SteeringWheelAngle(Degree value) {
        super(value, RANGE);
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
