package com.ford.openxc.measurements;

import com.ford.openxc.units.Unit;
import com.ford.openxc.util.Range;

public class Measurement<TheUnit extends Unit> {

    public boolean hasValue() {
        return false;
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

    public double getVariance() {
        return 0;
    }
}
