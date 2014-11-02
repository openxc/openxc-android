package com.openxc.units;

/**
 * A Meter is the base unit of length in the SI.
 */
public class Meter extends Quantity<Number> {
    private final String TYPE_STRING = "m";

    public Meter(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
