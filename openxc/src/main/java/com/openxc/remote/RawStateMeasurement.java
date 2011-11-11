package com.openxc.remote;

import android.os.Parcel;
import android.os.Parcelable;

public class RawStateMeasurement extends AbstractRawMeasurement<String> {
    public static final Parcelable.Creator<RawStateMeasurement> CREATOR =
            new Parcelable.Creator<RawStateMeasurement>() {
        public RawStateMeasurement createFromParcel(Parcel in) {
            return new RawStateMeasurement(in);
        }

        public RawStateMeasurement[] newArray(int size) {
            return new RawStateMeasurement[size];
        }
    };

    public RawStateMeasurement() { }

    public RawStateMeasurement(String value) {
        super(value);
    }

    private RawStateMeasurement(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getValue());
    }

    public void readFromParcel(Parcel in) {
        setValue(in.readString());
    }
}
