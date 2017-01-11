package com.openxc.units;

/**
 * The base interface for all values returned by a
 * {@link com.openxc.measurements.BaseMeasurement}.
 */
public abstract class Unit {
    public abstract Object getSerializedValue();

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        return obj != null && getClass() == obj.getClass();

    }
}
