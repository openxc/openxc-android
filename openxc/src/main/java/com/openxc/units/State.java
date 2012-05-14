package com.openxc.units;

import com.google.common.base.Objects;

/**
 * A State is a type of Unit with a limited number of acceptable values.
 */
public class State<T extends Enum<?>> implements Unit {
    private T mValue;

    /**
     * Construct an instance of State from the Enum T value.
     *
     * @param value an instance of the Enum T.
     */
    public State(T value) {
        mValue = value;
    }

    public boolean equalTo(T otherValue) {
        // TODO  this is incorrect - need to check cast and look at mValue
        return mValue.equals(otherValue);
    }

    /**
     * Return the value of this State as an Enum.
     *
     * This is primarily useful for comparison.
     *
     * @return this State's base Enum value.
     */
    public T enumValue() {
        return mValue;
    }

    public String getSerializedValue() {
        return mValue.toString();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", mValue)
            .toString();
    }
}
