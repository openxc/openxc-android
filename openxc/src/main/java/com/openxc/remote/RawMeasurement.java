package com.openxc.remote;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * An untyped measurement used only for the AIDL VehicleService interface.
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
 * This class implements the Parcelable interface, so it can be used directly as
 * a return value or function parameter in an AIDL interface.
 *
 * @see com.openxc.measurements.BaseMeasurement
 */
public class RawMeasurement implements Parcelable {
    private static final String TAG = "RawMeasurement";
    private static final String NAME_FIELD = "name";
    private static final String EVENT_FIELD = "event";
    private static final String VALUE_FIELD = "value";
    private static final String TIMESTAMP_FIELD = "timestamp";

    private double mTimestamp;
    private String mName;
    private Object mValue;
    private Object mEvent;

    public RawMeasurement(String name, Object value) {
        this();
        mName = name;
        mValue = value;
    }

    public RawMeasurement(String name, Object value, Object event) {
        this(name, value);
        mEvent = event;
    }

    private RawMeasurement(Parcel in) {
        readFromParcel(in);
    }

    private RawMeasurement() {
        mTimestamp = System.nanoTime();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(serialize());
    }

    public void readFromParcel(Parcel in) {
        RawMeasurement measurement = RawMeasurement.deserialize(
                in.readString());
        if(measurement != null) {
            copy(measurement);
        }
    }

    public static final Parcelable.Creator<RawMeasurement> CREATOR =
            new Parcelable.Creator<RawMeasurement>() {
        public RawMeasurement createFromParcel(Parcel in) {
            return new RawMeasurement(in);
        }

        public RawMeasurement[] newArray(int size) {
            return new RawMeasurement[size];
        }
    };

    public String serialize() {
        JSONObject message = new JSONObject();
        try {
            message.put(NAME_FIELD, getName());
            message.put(TIMESTAMP_FIELD, getTimestamp());
            message.put(VALUE_FIELD, getValue());
            message.putOpt(EVENT_FIELD, getEvent());
        } catch(JSONException e) {
            Log.w(TAG, "Unable to encode all data to JSON -- " +
                    "message may be incomplete", e);
        }
        return message.toString();
    }

    public static RawMeasurement deserialize(String measurementString) {
        JSONObject serializedMeasurement;
        try {
            serializedMeasurement = new JSONObject(measurementString);
        } catch(JSONException e) {
            Log.w(TAG, "Couldn't decode JSON from: " + measurementString);
            return null;
        }

        RawMeasurement measurement = new RawMeasurement();
        try {
            double timestamp = serializedMeasurement.optDouble(TIMESTAMP_FIELD);
            if(timestamp != Double.NaN) {
                measurement.mTimestamp = timestamp;
            }
            measurement.mName = serializedMeasurement.getString(NAME_FIELD);
            measurement.mValue = serializedMeasurement.get(VALUE_FIELD);
            measurement.mEvent = serializedMeasurement.opt(EVENT_FIELD);
        } catch(JSONException e) {
            Log.w(TAG, "JSON message didn't have the expected format: "
                    + serializedMeasurement, e);
        }
        return measurement;
    }

    public String getName() {
        return mName;
    }

    public Object getValue() {
        return mValue;
    }

    public boolean hasEvent() {
        return getEvent() != null;
    }

    public Object getEvent() {
        return mEvent;
    }

    public double getTimestamp() {
        return mTimestamp;
    }

    public int describeContents() {
        return 0;
    }

    private void copy(RawMeasurement measurement) {
        mTimestamp = measurement.getTimestamp();
        mName = measurement.getName();
        mValue = measurement.getValue();
        mEvent = measurement.getEvent();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", getValue())
            .add("event", getEvent())
            .toString();
    }
}
