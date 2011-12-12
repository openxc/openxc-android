package com.openxc.util;

import com.google.common.base.Objects;

import com.openxc.measurements.NoValueException;

import com.openxc.units.Unit;

/**
 * NoneData is a nullable object container.
 *
 * When you'd rather not return null from a function if there is no result,
 * return a NoneData with no value instead - it's still a valid object, but the
 * programmer can determine if it has a value before using it and avoid a
 * NullPointerException.
 */
public class NoneData<TheUnit extends Unit> {
    TheUnit mValue;

    /**
     * Construct an instance of NoneData with no value.
     */
    public NoneData() { }

    /**
     * Construct an instance of NoneData with the given value.
     *
     * @param value The object to wrap with an NoneData instance.
     */
    public NoneData(TheUnit value) {
        mValue = value;
    }

    /**
     * Check the validity of this instance.
     *
     * @return true if this instance has no value.
     */
    public boolean isNone() {
        return mValue == null;
    }

    /**
     * Return the value this instance wraps.
     *
     * @return The wrapped value (an instance of TheUnit)
     * @throws NoValueException if this instance has no value
     */
    public TheUnit getValue() throws NoValueException {
        if(isNone()) {
            throw new NoValueException();
        }
        return mValue;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", mValue)
            .add("isNone", isNone())
            .toString();
    }
}
