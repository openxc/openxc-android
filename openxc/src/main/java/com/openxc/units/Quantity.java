package com.openxc.units;

import com.google.common.base.Objects;

/**
 * A quantitative type of {@link Unit}.
 *
 * All quantitative children of {@link Unit} extend from this abstract class,
 * which encapsulates common logic for converting among different numerical
 * values.
 */
public abstract class Quantity<T extends Number> {
    private T mValue;

    /**
     * Construct an instance of Quantity with the value.
     *
     * @param value a quantitative Unit value.
     */
    public Quantity(T value) {
        mValue = value;
    }

    public boolean equalTo(T otherValue) {
        return mValue.equals(otherValue);
    }

    public double doubleValue() {
        return mValue.doubleValue();
    }

    public int intValue() {
        return mValue.intValue();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", mValue)
            .toString();
    }
}
