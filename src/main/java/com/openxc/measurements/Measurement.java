package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.Range;

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
