package com.openxc.util;

import com.google.common.base.Objects;

/**
 * A Range is a pair of T values that represent a range of values.
 */
public class Range<T> {
    private T mMin;
    private T mMax;

    /**
     * Construct an instance of Range with a min and max value.
     *
     * @param min The minimum value for the range.
     * @param max The maximum value for the range.
     */
    public Range(T min, T max) {
        mMin = min;
        mMax = max;
    }

    public T getMin() {
        return mMin;
    }

    public T getMax() {
        return mMax;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Range<?>)  {
            @SuppressWarnings("unchecked")
            final Range<T> that = (Range<T>) object;
            return this == that || (
                    that.getMin() == getMin() &&
                    that.getMax() == getMax());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mMin.hashCode() + mMax.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("min", getMin())
            .add("max", getMax())
            .toString();
    }
}
