package com.openxc.units;

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
}
