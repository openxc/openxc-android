package com.openxc.remote;

import java.io.IOException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.base.Objects;
import com.openxc.BinaryMessages;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.serializers.JsonSerializer;


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

    private String mCachedSerialization;
    private long mTimestamp;
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

    public RawMeasurement(String name, Object value, Object event,
            long timestamp) {
        this(name, value, event);
        mTimestamp = timestamp;
        timestamp();
    }

    public RawMeasurement(String serialized)
            throws UnrecognizedMeasurementTypeException {
        deserialize(serialized, this);
        timestamp();
    }

    public RawMeasurement(BinaryMessages.VehicleMessage message)
            throws UnrecognizedMeasurementTypeException {
        deserialize(message, this);
        timestamp();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getName());
        out.writeLong(getTimestamp());
        out.writeValue(getValue());
        out.writeValue(getEvent());
    }

    public void readFromParcel(Parcel in) {
        mName = in.readString();
        mTimestamp = in.readLong();
        mValue = in.readValue(null);
        mEvent = in.readValue(null);
    }

    public static final Parcelable.Creator<RawMeasurement> CREATOR =
            new Parcelable.Creator<RawMeasurement>() {
        public RawMeasurement createFromParcel(Parcel in) {
            try {
                return new RawMeasurement(in);
            } catch(UnrecognizedMeasurementTypeException e) {
                return new RawMeasurement();
            }
        }

        public RawMeasurement[] newArray(int size) {
            return new RawMeasurement[size];
        }
    };

    public String serialize() {
        return serialize(false);
    }

    public String serialize(boolean reserialize) {
        if(reserialize || mCachedSerialization == null) {
            mCachedSerialization = JsonSerializer.serialize(getName(),
                    getValue(), getEvent(), getTimestamp());
        }
        return mCachedSerialization;
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

    /**
     * @return true if the measurement has a valid timestamp.
     */
    public boolean isTimestamped() {
        return getTimestamp() != 0;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * Make the measurement's timestamp invalid so it won't end up in the
     * serialized version.
     */
    public void untimestamp() {
        mTimestamp = 0;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("value", getValue())
            .add("event", getEvent())
            .toString();
    }

    private static void deserialize(BinaryMessages.VehicleMessage message,
            RawMeasurement measurement)
            throws UnrecognizedMeasurementTypeException {
        if(message.hasTranslatedMessage()) {
            BinaryMessages.TranslatedMessage translatedMessage = message.getTranslatedMessage();
            if(translatedMessage.hasName()) {
                measurement.mName = translatedMessage.getName();
            } else {
                throw new UnrecognizedMeasurementTypeException(
                        "Binary message is missing name");
            }

            if(translatedMessage.hasNumericValue()) {
                measurement.mValue = translatedMessage.getNumericValue();
            } else if(translatedMessage.hasBooleanValue()) {
                measurement.mValue = translatedMessage.getBooleanValue();
            } else if(translatedMessage.hasStringValue()) {
                measurement.mValue = translatedMessage.getStringValue();
            } else {
                throw new UnrecognizedMeasurementTypeException(
                        "Binary message had no value");
            }

            if(translatedMessage.hasNumericEvent()) {
                measurement.mEvent = translatedMessage.getNumericEvent();
            } else if(translatedMessage.hasBooleanEvent()) {
                measurement.mEvent = translatedMessage.getBooleanEvent();
            } else if(translatedMessage.hasStringEvent()) {
                measurement.mEvent = translatedMessage.getStringEvent();
            }
        } else if(message.hasRawMessage()) {
            // TODO
        } else {
            throw new UnrecognizedMeasurementTypeException(
                    "Binary message type not recognized");
        }
    }

    private static void deserialize(String measurementString,
            RawMeasurement measurement)
            throws UnrecognizedMeasurementTypeException {
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser;
        try {
            parser = jsonFactory.createParser(measurementString);
        } catch(IOException e) {
            String message = "Couldn't decode JSON from: " + measurementString;
            Log.w(TAG, message, e);
            throw new UnrecognizedMeasurementTypeException(message, e);
        }

        try {
            parser.nextToken();
            while(parser.nextToken() != JsonToken.END_OBJECT) {
                String field = parser.getCurrentName();
                parser.nextToken();
                if(JsonSerializer.NAME_FIELD.equals(field)) {
                    measurement.mName = parser.getText();
                } else if(JsonSerializer.VALUE_FIELD.equals(field)) {
                    measurement.mValue = parseUnknownType(parser);
                } else if(JsonSerializer.EVENT_FIELD.equals(field)) {
                    measurement.mEvent = parseUnknownType(parser);
                } else if(JsonSerializer.TIMESTAMP_FIELD.equals(field)) {
                    // serialized measurements record the timestamp in seconds
                    // with fractional parts - we multiply to get pure
                    // milliseconds and then chop off anything more precise.
                    measurement.mTimestamp =
                        (long) (parser.getNumberValue().doubleValue() * 1000);
                } else {
                    throw new UnrecognizedMeasurementTypeException(
                            "Bad field in: " + measurementString);
                }
            }

            if(measurement.mName == null) {
                throw new UnrecognizedMeasurementTypeException(
                        "Missing name in: " + measurementString);
            }
            if(measurement.mValue == null) {
                throw new UnrecognizedMeasurementTypeException(
                        "Missing value in: " + measurementString);
            }
        } catch(IOException e) {
            String message = "JSON message didn't have the expected format: "
                    + measurementString;
            Log.w(TAG, message, e);
            throw new UnrecognizedMeasurementTypeException(message, e);
        }
        measurement.mCachedSerialization = measurementString;
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

    private RawMeasurement(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
        timestamp();
    }

    private RawMeasurement() {
        timestamp();
    }

    private void timestamp() {
        if(!isTimestamped()) {
            mTimestamp = System.currentTimeMillis();
        }
    }
}
