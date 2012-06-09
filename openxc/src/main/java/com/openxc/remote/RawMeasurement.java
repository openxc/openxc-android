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
 * All OpenXC measurements need to be representable by a double so they can be
 * easily fit through the AIDL interface to VehicleService. This class
 * shouldn't be used anywhere else becuase hey, types are important.
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

    public static final Parcelable.Creator<RawMeasurement> CREATOR =
            new Parcelable.Creator<RawMeasurement>() {
        public RawMeasurement createFromParcel(Parcel in) {
            return new RawMeasurement(in);
        }

        public RawMeasurement[] newArray(int size) {
            return new RawMeasurement[size];
        }
    };

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
        out.writeString(message.toString());
    }

    public void readFromParcel(Parcel in) {
        String measurementString = in.readString();
        JSONObject serializedMeasurement;
        try {
            serializedMeasurement = new JSONObject(measurementString);
        } catch(JSONException e) {
            Log.w(TAG, "Couldn't decode JSON from: " + measurementString);
            return;
        }

        try {
            mTimestamp = serializedMeasurement.getDouble(TIMESTAMP_FIELD);
            mName = serializedMeasurement.getString(NAME_FIELD);
            mValue = serializedMeasurement.get(VALUE_FIELD);
            mEvent = serializedMeasurement.opt(EVENT_FIELD);
        } catch(JSONException e) {
            Log.w(TAG, "JSON message didn't have the expected format: "
                    + serializedMeasurement, e);
        }
    }

    public String getName() {
        return mName;
    }

    public boolean isValid() {
        return getValue() != null;
    }

    public int describeContents() {
        return 0;
    }

    public Object getValue() {
        return mValue;
    }

    public Object getEvent() {
        return mEvent;
    }

    public boolean hasEvent() {
        return getEvent() != null;
    }

    public double getTimestamp() {
        return mTimestamp;
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
