package com.openxc.units;

/**
 * A Liter is a metric system unit of volume.
 */
public class Liter extends Quantity<Number> {
    private final String TYPE_STRING = "L";

    public Liter(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
