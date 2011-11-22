package com.openxc.units;

import com.google.common.base.Objects;

public class Boolean implements Unit {
    private boolean mValue;

    public Boolean(boolean value) {
        mValue = value;
    }

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
