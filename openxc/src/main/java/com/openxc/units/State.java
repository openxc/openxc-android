package com.openxc.units;

public class State<T extends Enum<?>> implements Unit {
    private T mValue;

    public State(T value) {
        mValue = value;
    }

    public boolean equalTo(T otherValue) {
        return mValue.equals(otherValue);
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
