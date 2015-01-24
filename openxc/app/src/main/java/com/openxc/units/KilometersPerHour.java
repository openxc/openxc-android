package com.openxc.units;

/**
 * KilometersPerHour is an SI derived unit of velocity.
 */
public class KilometersPerHour extends Quantity<Number> {
    private final String TYPE_STRING = "km / h";

    public KilometersPerHour(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
