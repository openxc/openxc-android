package com.openxc.units;

/**
 * The base interface for all values returned by a
 * {@link com.openxc.measurements.BaseMeasurement}.
 */
public interface Unit {
    public Object getSerializedValue();
}
