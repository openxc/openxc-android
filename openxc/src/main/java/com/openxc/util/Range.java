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
}
