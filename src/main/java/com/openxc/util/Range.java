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
}
