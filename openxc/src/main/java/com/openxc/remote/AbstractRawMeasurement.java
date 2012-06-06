package com.openxc.remote;

import com.google.common.base.Objects;

import android.os.Parcel;


/**
 * The base class for all raw measurements.
 *
 * This abstract base class is intented to be the parent of numerical, state and
 * boolean measurements. The architecture ended up using only numerical
 * measurements, with other types being coerced to doubles.
 *
 * A raw measurement can have a value, an event, both or neither. Most
 * measurements have only a value - measurements also with an event include
 * things like button events (where both the button direction and action need to
 * be identified). The value and event are both nullable, for cases where a
 * measurement needs to be returned but there is no valid value for it.
 *
 * TODO Should we keep this in case the architecture is reworked, or abandon it
 * and combine this with RawMeasurement?
 */
public abstract class AbstractRawMeasurement<TheValueUnit, TheEventUnit> {
    protected double mTimestamp;
    protected TheValueUnit mValue;
    protected TheEventUnit mEvent;

    public AbstractRawMeasurement() {
        mTimestamp = System.nanoTime();
    }

    public AbstractRawMeasurement(TheValueUnit value) {
        this();
        mValue = value;
    }

    public AbstractRawMeasurement(TheValueUnit value, TheEventUnit event) {
        this(value);
        mEvent = event;
    }

    public boolean isValid() {
        return getValue() != null;
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

    protected void setTimestamp(double timestamp) {
        mTimestamp = timestamp;
    }

    public boolean hasEvent() {
        return getEvent() != null;
    }

    public double getTimestamp() {
        return mTimestamp;
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
