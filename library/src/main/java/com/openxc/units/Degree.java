package com.openxc.units;

/**
 * Degree, a unit angle of measurement or in a coordinate system.
 *
 * TODO this shouldn't represent two things.
 */
public class Degree extends Quantity<Number> {
    private final String TYPE_STRING = "\u00B0";

    public Degree(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
