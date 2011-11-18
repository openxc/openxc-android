package com.openxc.units;

public class Boolean implements Unit {
    private boolean mValue;

    public Boolean(boolean value) {
        mValue = value;
    }

    public Boolean(Double value) {
        mValue = value == 1;
    }

    @Override
    public String toString() {
        return "value: " + mValue;
    }

    public boolean booleanValue() {
        return mValue;
    }
}
