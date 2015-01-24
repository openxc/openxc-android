package com.openxc.units;

/**
 * Kilometer is an SI unit of distance.
 */
public class Kilometer extends Quantity<Number> {
    private final String TYPE_STRING = "km";

    public Kilometer(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
