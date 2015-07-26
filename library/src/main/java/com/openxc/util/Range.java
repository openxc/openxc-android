package com.openxc.util;

import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

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
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Range<T> that = (Range<T>) obj;
        return Objects.equal(getMin(), that.getMin()) &&
                Objects.equal(getMax(), that.getMax());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mMin, mMax);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("min", getMin())
            .add("max", getMax())
            .toString();
    }
}
