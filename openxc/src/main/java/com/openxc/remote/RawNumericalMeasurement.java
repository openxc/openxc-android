package com.openxc.remote;

import android.os.Parcel;
import android.os.Parcelable;

import android.util.Log;

public class RawNumericalMeasurement extends AbstractRawMeasurement<Double>
        implements Parcelable {
    public static final Parcelable.Creator<RawNumericalMeasurement> CREATOR =
            new Parcelable.Creator<RawNumericalMeasurement>() {
        public RawNumericalMeasurement createFromParcel(Parcel in) {
            return new RawNumericalMeasurement(in);
        }

        public RawNumericalMeasurement[] newArray(int size) {
            return new RawNumericalMeasurement[size];
        }
    };

    public RawNumericalMeasurement() {
        super();
    }

    public RawNumericalMeasurement(Double value) {
        super(value);
    }

    private RawNumericalMeasurement(Parcel in) {
        readFromParcel(in);
    }

    public boolean isValid() {
        return super.isValid() && !getValue().isNaN();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(getValue().doubleValue());
    }

    public void readFromParcel(Parcel in) {
        setValue(new Double(in.readDouble()));
    }
}
