package com.openxc.units;

/**
 * A quantitative type of {@link Unit}.
 *
 * All quantitative children of {@link Unit} extend from this abstract class,
 * which encapsulates common logic for converting among different numerical
 * values.
 */
public abstract class Quantity<T extends Number> extends Unit {
    private T mValue;

    /**
     * Construct an instance of Quantity with the value.
     *
     * @param value a quantitative Unit value.
     */
    public Quantity(T value) {
        mValue = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Quantity<T> other = (Quantity<T>) obj;
        return mValue.equals(other.mValue);
    }

    public double doubleValue() {
        return mValue.doubleValue();
    }

    @Override
    public Object getSerializedValue() {
        return doubleValue();
    }

    public int intValue() {
        return mValue.intValue();
    }

    public String getTypeString() {
        return "";
    }

    @Override
    public String toString() {
        return mValue.toString() + " " + getTypeString();
    }
}
