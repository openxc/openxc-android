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
    /**
     * Check the validity of this measurement.
     *
     * @return true if this measurement has no value.
     */
    public boolean isNone();

    /**
     * Retreive the age of this measurement.
     *
     * @return the age of the data in seconds.
     */
    public double getAge() throws NoValueException;

    /**
     * Determine if this measurement has a valid range.
     *
     * @return true if the measurement has a non-null range.
     */
    public boolean hasRange();

    /**
     * Retrieve the valid range of the measurement.
     *
     * @return the Range of the measurement
     * @throws NoRangeException if the measurement doesn't have a range.
     */
    public Range<? extends Unit> getRange() throws NoRangeException;

    /**
     * Return the value of this measurement.
     *
     * @return The wrapped value (an instance of TheUnit)
     * @throws NoValueException if this measurement has no value
     */
    public Unit getValue() throws NoValueException;
}
