package com.openxc.units;

public abstract class Quantity<T> {
    private T mValue;

    public Quantity(T value) {
        mValue = value;
    }

    public boolean equalTo(T otherValue) {
        return mValue.equals(otherValue);
    }

    public double doubleValue() {
        return 0.0;
    }

    public int intValue() {
        return 0;
    }
}
