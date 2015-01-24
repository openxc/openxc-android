package com.openxc.units;

import java.util.Locale;

/**
 * A State is a type of Unit with a limited number of acceptable values.
 */
public class State<T extends Enum<?>> extends Unit {
    private T mValue;

    /**
     * Construct an instance of State from the Enum T value.
     *
     * @param value an instance of the Enum T.
     */
    public State(T value) {
        mValue = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final State<T> other = (State<T>) obj;
        return mValue.equals(other.mValue);
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

    @Override
    public String getSerializedValue() {
        return mValue.toString().toLowerCase(Locale.US);
    }

    @Override
    public String toString() {
        return mValue.toString();
    }
}
