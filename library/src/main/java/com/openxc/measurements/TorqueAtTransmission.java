package com.openxc.measurements;

import com.openxc.units.NewtonMeter;
import com.openxc.util.Range;

/**
 * The TorqueAtTransmission is the actual current torque in the transmission.
 */
public class TorqueAtTransmission extends BaseMeasurement<NewtonMeter> {
    private final static Range<NewtonMeter> RANGE = new Range<>(
            new NewtonMeter(-500), new NewtonMeter(1500));
    public final static String ID = "torque_at_transmission";

    public TorqueAtTransmission(NewtonMeter value) {
        super(value, RANGE);
    }

    public TorqueAtTransmission(Number value) {
        this(new NewtonMeter(value));
    }

    @Override
    public String getGenericName() {
        return ID;
    }
}
