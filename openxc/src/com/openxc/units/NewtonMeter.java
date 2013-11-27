package com.openxc.units;

/**
 * A NewtonMeter is a unit of torque.
 */
public class NewtonMeter extends Quantity<Number> {
    private final String TYPE_STRING = "Nm";

    public NewtonMeter(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
