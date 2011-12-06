package com.openxc.remote;

import com.google.common.base.Objects;

import android.os.Parcel;

public abstract class AbstractRawMeasurement<TheValueUnit, TheEventUnit> {
    protected TheValueUnit mValue;
    protected TheEventUnit mEvent;

    public AbstractRawMeasurement() { }

    public AbstractRawMeasurement(TheValueUnit value) {
        mValue = value;
    }

    public AbstractRawMeasurement(TheValueUnit value, TheEventUnit event) {
        this(value);
        mEvent = event;
    }

    public boolean isValid() {
        return mValue != null;
    }

    public int describeContents() {
        return 0;
    }

    public TheValueUnit getValue() {
        return mValue;
    }

    public void setValue(TheValueUnit value) {
        mValue = value;
    }

    public TheEventUnit getEvent() {
        return mEvent;
    }

    public void setEvent(TheEventUnit event) {
        mEvent = event;
    }

    public boolean hasEvent() {
        return mEvent != null;
    }

    public abstract void writeToParcel(Parcel out, int flags);

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", getValue())
            .add("event", getEvent())
            .add("valid", isValid())
            .toString();
    }
}
