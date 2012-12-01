package com.openxc.remote;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import com.google.common.base.Objects;

import com.openxc.measurements.serializers.JsonSerializer;

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
        timestamp();
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
        Double timestamp = isTimestamped() ? getTimestamp() : null;
        return JsonSerializer.serialize(getName(), getValue(), getEvent(),
                timestamp);
    }

    // TODO I think there was a reason I had this return null instead of
    // throwing an exception, but that should probably be revisited because in
    // general it's not good practice.
    public static RawMeasurement deserialize(String measurementString) {
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser;
        try {
            parser = jsonFactory.createParser(measurementString);
        } catch(IOException e) {
            Log.w(TAG, "Couldn't decode JSON from: " + measurementString, e);
            return null;
        }

        RawMeasurement measurement = new RawMeasurement();
        try {
            parser.nextToken();
            while(parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                if(JsonSerializer.NAME_FIELD.equals(field)) {
                    measurement.mName = parser.getText();
                } else if(JsonSerializer.VALUE_FIELD.equals(field)) {
                    // TODO
                    measurement.mValue = parseUnknownType(parser);
                } else if(JsonSerializer.EVENT_FIELD.equals(field)) {
                    // TODO
                    measurement.mEvent = parseUnknownType(parser);
                } else if(JsonSerializer.TIMESTAMP_FIELD.equals(field)) {
                    measurement.mTimestamp =
                        parser.getNumberValue().doubleValue();
                }
            }
        } catch(IOException e) {
            Log.w(TAG, "JSON message didn't have the expected format: "
                    + measurementString, e);
            return null;
        }

        return measurement;
    }

    private static Object parseUnknownType(JsonParser parser) {
        Object value = null;
        try {
            value = parser.getNumberValue();
        } catch(JsonParseException e) {
            try {
                value = parser.getBooleanValue();
            } catch(JsonParseException e2) {
                try {
                    value = parser.getText();
                } catch(JsonParseException e3) {
                } catch(IOException e4) {
                }
            } catch(IOException e5) {
            }
        } catch(IOException e) {
        }
        return value;
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

    public boolean isTimestamped() {
        return getTimestamp() != null && !Double.isNaN(getTimestamp());
    }
    public Double getTimestamp() {
        return mTimestamp;
    }

    public void timestamp() {
        mTimestamp = System.currentTimeMillis() / 1000.0;
    }

    public void untimestamp() {
    	mTimestamp = Double.NaN;
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
