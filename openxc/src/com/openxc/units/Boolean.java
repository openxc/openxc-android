package com.openxc.units;

/**
 * A boolean type of Unit.
 *
 * This class handles converting to a boolean from numerical measurements.
 */
public class Boolean extends Unit {
    private boolean mValue;

    /**
     * Construct a new Boolean from the given value.
     *
     * @param value the boolean value.
     */
    public Boolean(boolean value) {
        mValue = value;
    }

    /**
     * Construct a Boolean from the numerical value.
     *
     * @param value If the value == 1, the constructed object will be true.
     */
    public Boolean(Number value) {
        mValue = value.intValue() == 1;
    }

    public boolean booleanValue() {
        return mValue;
    }

    @Override
    public java.lang.Boolean getSerializedValue() {
        return new java.lang.Boolean(booleanValue());
    }

    @Override
    public String toString() {
        return mValue ? "on" : "off";
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) {
            return false;
        }

        final Boolean other = (Boolean) obj;
        return mValue == other.mValue;
    }
}
