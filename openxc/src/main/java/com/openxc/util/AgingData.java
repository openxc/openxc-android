package com.openxc.util;

import com.google.common.base.Objects;

import com.openxc.measurements.NoValueException;

import com.openxc.units.Unit;

public class AgingData<TheUnit extends Unit> extends NoneData<TheUnit> {
    private double mBornTime;

    public AgingData(TheUnit unit) {
        super(unit);
        mBornTime = System.nanoTime();
    }

    public AgingData() {
        super();
        mBornTime = System.nanoTime();
    }

    public double getAge() throws NoValueException {
        if(isNone()) {
            throw new NoValueException();
        }
        return (System.nanoTime() - mBornTime) / 1000000000.0;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("born", mBornTime)
            .toString();
    }
}
