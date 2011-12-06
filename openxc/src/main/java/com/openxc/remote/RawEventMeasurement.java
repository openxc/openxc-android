package com.openxc.remote;

import com.google.common.base.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class RawEventMeasurement extends RawMeasurement {
    protected Double mEvent;

    public static final Parcelable.Creator<RawEventMeasurement> CREATOR =
            new Parcelable.Creator<RawEventMeasurement>() {
        public RawEventMeasurement createFromParcel(Parcel in) {
            return new RawEventMeasurement(in);
        }

        public RawEventMeasurement[] newArray(int size) {
            return new RawEventMeasurement[size];
        }
    };

    public RawEventMeasurement() {
        super();
    }

    public RawEventMeasurement(Double value) {
        super(value);
    }

    public RawEventMeasurement(Double value, Double event) {
        super(value);
        mEvent = event;
    }

    private RawEventMeasurement(Parcel in) {
        readFromParcel(in);
    }

    public Double getEvent() {
        return mEvent;
    }

    public void setEvent(Double event) {
        mEvent = event;
    }


    public boolean isValid() {
        boolean valid = super.isValid() && !getValue().isNaN();
        if(mEvent != null) {
            valid = valid && !getEvent().isNaN();
        }
        return valid;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(getValue().doubleValue());
        out.writeDouble(getEvent().doubleValue());
    }

    public void readFromParcel(Parcel in) {
        setValue(new Double(in.readDouble()));
        setEvent(new Double(in.readDouble()));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", getValue())
            .add("event", getEvent())
            .add("valid", isValid())
            .toString();
    }
}
