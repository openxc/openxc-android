package com.openxc.units;

import com.google.common.base.Objects;

/**
 * A boolean type of Unit.
 *
 * This class handles converting to a boolean from numerical measurements.
 */
public class Boolean implements Unit {
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
    public Boolean(Double value) {
        mValue = value == 1;
    }

    public boolean booleanValue() {
        return mValue;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", booleanValue())
            .toString();
    }
}
