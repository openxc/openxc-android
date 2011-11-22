package com.openxc.units;

import com.google.common.base.Objects;

public abstract class Quantity<T extends Number> {
    private T mValue;

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
