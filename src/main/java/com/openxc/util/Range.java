package com.openxc.util;

public class Range<T> {
    private T mMin;
    private T mMax;

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
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }
        if(other == null) {
            return false;
        }
        if(getClass() != other.getClass()) {
            return false;

        }
        final Range<T> otherRange = (Range<T>) other;
        return otherRange.getMin() == getMin() && otherRange.getMax() == getMax();
    }

    @Override
    public int hashCode() {
        return mMin.hashCode() + mMax.hashCode();
    }
}
