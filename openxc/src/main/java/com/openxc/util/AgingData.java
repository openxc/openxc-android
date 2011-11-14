package com.openxc.util;

import com.openxc.measurements.NoValueException;

import com.openxc.units.Unit;

public class AgingData<TheUnit extends Unit> extends NoneData<TheUnit> {
    private double mBornTime;

    public AgingData(TheUnit unit) {
        super(unit);
    }

    public AgingData() {
        super();
        mBornTime = System.nanoTime();
    }

    public double getAge() throws NoValueException {
        if(!isNone()) {
            throw new NoValueException();
        }
        return System.nanoTime() - mBornTime;
    }
}
