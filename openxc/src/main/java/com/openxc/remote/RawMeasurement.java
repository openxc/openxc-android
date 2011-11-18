package com.openxc.remote;

import android.os.Parcel;
import android.os.Parcelable;

public class RawMeasurement extends AbstractRawMeasurement<Double>
        implements Parcelable {
    public static final Parcelable.Creator<RawMeasurement> CREATOR =
            new Parcelable.Creator<RawMeasurement>() {
        public RawMeasurement createFromParcel(Parcel in) {
            return new RawMeasurement(in);
        }

        public RawMeasurement[] newArray(int size) {
            return new RawMeasurement[size];
        }
    };

    public RawMeasurement() {
        super();
    }

    public RawMeasurement(Double value) {
        super(value);
    }

    private RawMeasurement(Parcel in) {
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
