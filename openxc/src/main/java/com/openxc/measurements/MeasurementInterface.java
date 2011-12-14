package com.openxc.measurements;

import com.openxc.units.Unit;
import com.openxc.util.Range;

/**
 * The MeasurementInterface is the base for all OpenXC measurements.
 *
 * A Measurement has at least a value and an age, and optionally a range of
 * valid values. It is not required to have a value - users of this class must
 * either catch the NoValueException returned by {@link #getValue()} or check for
 * validity first with {@link #isNone()}.
 */
public interface MeasurementInterface {
    public boolean isNone();
    public double getAge() throws NoValueException;
    public boolean hasRange();
    public Range<? extends Unit> getRange() throws NoRangeException;
    public Unit getValue() throws NoValueException;
}
