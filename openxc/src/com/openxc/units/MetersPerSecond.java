package com.openxc.units;

/**
 * MetersPerSecond is an SI derived unit of velocity.
 */
public class MetersPerSecond extends Quantity<Number> {
    private final String TYPE_STRING = "m / s";

    public MetersPerSecond(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
