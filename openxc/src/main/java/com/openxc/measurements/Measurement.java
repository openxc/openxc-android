package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.AgingData;
import com.openxc.util.Range;

public class Measurement<TheUnit extends Unit> implements MeasurementInterface {
    private AgingData<TheUnit> mValue;
    private Range<TheUnit> mRange;

    public Measurement() {
        mValue = new AgingData<TheUnit>();
    }

    public Measurement(TheUnit value) {
        mValue = new AgingData<TheUnit>(value);
    }

    public Measurement(TheUnit value, Range<TheUnit> range) {
        this(value);
        mRange = range;
    }

    public double getAge() throws NoValueException {
        return mValue.getAge();
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
        return 0.0;
    }

    public TheUnit getValue() throws NoValueException {
        return mValue.getValue();
    }

    public boolean isNone() {
        return mValue.isNone();
    }
}
