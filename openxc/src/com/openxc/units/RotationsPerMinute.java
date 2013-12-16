package com.openxc.units;

/**
 * RotationsPerMinute is a measure of the frequency of a rotation.
 *
 * This is commonly used with regard to engine speed (i.e. RPM).
 */
public class RotationsPerMinute extends Quantity<Number> {
    private final String TYPE_STRING = "RPM";

    public RotationsPerMinute(Number value) {
        super(value);
    }

    @Override
    public String getTypeString() {
        return TYPE_STRING;
    }
}
