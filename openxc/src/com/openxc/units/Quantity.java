package com.openxc.units;

import com.google.common.base.Objects;

/**
 * A quantitative type of {@link Unit}.
 *
 * All quantitative children of {@link Unit} extend from this abstract class,
 * which encapsulates common logic for converting among different numerical
 * values.
 */
public abstract class Quantity<T extends Number> extends Unit {
    private T mValue;

    /**
     * Construct an instance of Quantity with the value.
     *
     * @param value a quantitative Unit value.
     */
    public Quantity(T value) {
        mValue = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) {
            return false;
        }

        @SuppressWarnings("unchecked")
		final Quantity<T> other = (Quantity<T>) obj;
        return mValue.equals(other.mValue);
    }

    public double doubleValue() {
        return mValue.doubleValue();
    }

    public Object getSerializedValue() {
        return doubleValue();
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
