package com.openxc.util;

import com.google.common.base.Objects;

import com.openxc.measurements.NoValueException;

import com.openxc.units.Unit;

public class NoneData<TheUnit extends Unit> {
    TheUnit mValue;

    public NoneData() { }

    public NoneData(TheUnit value) {
        mValue = value;
    }

    public boolean isNone() {
        return mValue == null;
    }

    public TheUnit getValue() throws NoValueException {
        if(isNone()) {
            throw new NoValueException();
        }
        return mValue;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", mValue)
            .add("isNone", isNone())
            .toString();
    }
}
