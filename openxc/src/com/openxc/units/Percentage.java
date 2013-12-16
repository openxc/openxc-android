package com.openxc.units;

/**
 * Percentage, a unit expressing a number as a fraction of 100.
 */
public class Percentage extends Quantity<Number> {
    private final String TYPE_STRING = "%";

    public Percentage(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
