package com.openxc.measurements;

import com.google.common.base.Objects;

import com.openxc.units.Unit;
import com.openxc.util.AgingData;
import com.openxc.util.Range;

/**
 * The Measurement is the base implementation of the MeasurementInterface, and
 * wraps wraps an instance of a {@link Unit}, and the value it returns is always
 * in terms of this Unit.
 *
 * The Unit wrapper might seem annoying at first, but it is critical to avoid
 * misinterpreting the unit and crashing your lander into Mars
 * (http://en.wikipedia.org/wiki/Mars_Climate_Orbiter).
 *
 * Most applications will not use this class directly, but will import specific
 * child classes that correspond to specific types of measurements - i.e. the
 * parameterized instances of this class with a Unit. That may seem like a
 * "psuedo-typedef" but we're using it it to enforce the binding between
 * the measurement and its unit type. This unfortunately means we have to add
 * constructors to every child class because they aren't inherited from
 * Measurement. If you know of a better way, please say so.
 *
 *
 * All subclasses must have a public static String field named ID to be used
 * with the OpenXC vehicle services - this is unfortunately not enforced by the
 * class hierarchy.
 */
public class Measurement<TheUnit extends Unit> implements MeasurementInterface {
    private AgingData<TheUnit> mValue;
    private Range<TheUnit> mRange;

    /**
     * Construct a new Measurement with the given value.
     *
     * @param value the TheUnit this measurement represents.
     */
    public Measurement(TheUnit value) {
        mValue = new AgingData<TheUnit>(value);
    }

    /**
     * Construct an new Measurement with the gievn value and valid Range.
     *
     * There is not currently any automated verification that the value is
     * within the range - this is up to the application programmer.
     *
     * @param value the TheUnit this measurement represents.
     * @param range the valid {@link Range} of values for this measurement.
     */
    public Measurement(TheUnit value, Range<TheUnit> range) {
        this(value);
        mRange = range;
    }

    public double getAge() {
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

    public TheUnit getValue() {
        return mValue.getValue();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", mValue)
            .add("range", mRange)
            .toString();
    }
}
