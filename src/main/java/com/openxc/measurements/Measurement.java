package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.Range;

public class Measurement<TheUnit extends Unit> {
    private TheUnit mValue;

    public boolean hasValue() {
        return mValue != null;
    }

    public int getAge() {
        return 0;
    }

    public boolean hasRange() {
        return true;
    }

    public Range<Double> getRange() {
        return new Range<Double>(0.0, 0.0);
    }

    public TheUnit getVariance() {
        return null;
    }

    public TheUnit getValue() throws NoValueException {
        if(!hasValue()) {
            throw new NoValueException();
        }
        return mValue;
    }
}
