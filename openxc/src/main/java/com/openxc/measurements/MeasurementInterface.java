package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.Range;

public interface MeasurementInterface {
    public boolean isNone();
    public double getAge() throws NoValueException;
    public boolean hasRange();
    public Range<? extends Unit> getRange() throws NoRangeException;
    public Unit getVariance();
    public Unit getValue() throws NoValueException;
}
