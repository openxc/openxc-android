package com.openxc.measurements;

import com.openxc.units.NewtonMeter;
import com.openxc.util.Range;

/**
 * The PowertrainTorque is the current actual torque available at the
 * powertrain.
 */
public class PowertrainTorque extends Measurement<NewtonMeter>
        implements VehicleMeasurement {
    private final static Range<NewtonMeter> RANGE = new Range<NewtonMeter>(
            new NewtonMeter(-500), new NewtonMeter(1500));
    public final static String ID = "powertrain_torque";

    public PowertrainTorque() { }

    public PowertrainTorque(Double value) {
        super(new NewtonMeter(value), RANGE);
    }

    public PowertrainTorque(NewtonMeter value) {
        super(value, RANGE);
    }
}
