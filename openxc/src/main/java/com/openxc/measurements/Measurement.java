package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.Range;

public class Measurement<TheUnit extends Unit> {
    private TheUnit mValue;
    private Range<TheUnit> mRange;
    private double mBornTime;

    public Measurement() {
        mBornTime = System.nanoTime();
    }

    public Measurement(TheUnit value) {
        this();
        mValue = value;
    }

    public Measurement(TheUnit value, Range<TheUnit> range) {
        this(value);
        mRange = range;
    }

    public boolean hasValue() {
        return mValue != null;
    }

    public double getAge() throws NoValueException {
        if(!hasValue()) {
            throw new NoValueException();
        }
        return System.nanoTime() - mBornTime;
    }

    public boolean hasRange() {
        return mRange != null;
    }

    public Range<TheUnit> getRange() throws NoRangeException {
        if(!hasRange()) {
            throw new NoRangeException();
        }
        return mRange;
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
