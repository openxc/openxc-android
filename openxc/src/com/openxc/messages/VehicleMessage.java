package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.base.Objects;

public class VehicleMessage implements Parcelable {
    private static final String TAG = "VehicleMessage";
    public static final String TIMESTAMP_KEY = "timestamp";

    private long mTimestamp;
    private Map<String, Object> mValues = new HashMap<String, Object>();

    public VehicleMessage() {
        timestamp();
    }

    /**
     * @param timestamp timestamp as milliseconds since unix epoch
     */
    public VehicleMessage(Long timestamp) throws InvalidMessageFieldsException {
        this();
        setTimestamp(timestamp);
    }

    public VehicleMessage(Long timestamp, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        this(values);
        setTimestamp(timestamp);
    }

    /**
     * @param values A map of all fields and values in this message. If the map
     * contains the key TIMESTAMP_KEY, its value is interpreted as a UNIX
     * timestamp (seconds since the UNIX epoch, as a double with millisecond
     * precision).
     */
    public VehicleMessage(Map<String, Object> values) {
        this();
        if(values != null) {
            mValues = new HashMap<String, Object>(values);
        }

        if(contains(TIMESTAMP_KEY)) {
            double timestampSeconds = (Double) getValuesMap().remove(TIMESTAMP_KEY);
            mTimestamp = (long) timestampSeconds * 1000;
        }
    }

    public void setTimestamp(Long timestamp)
            throws InvalidMessageFieldsException {
        if(timestamp == null) {
            throw new InvalidMessageFieldsException(
                    "Explicit timestmap cannot be null");
        }
        mTimestamp = timestamp;
    }

    public static VehicleMessage buildSubtype(Map<String, Object> values)
            throws UnrecognizedMessageTypeException {
        // Must check from most specific to least
        VehicleMessage message = new VehicleMessage();
        // TODO could clean this up with reflection since they all now have the
        // same constructor
        try {
            if(CanMessage.containsAllRequiredFields(values)) {
                message = new CanMessage(values);
            } else if(DiagnosticResponse.containsAllRequiredFields(values)) {
                message = new DiagnosticResponse(values);
            } else if(DiagnosticRequest.containsAllRequiredFields(values)) {
                message = new DiagnosticRequest(values);
            } else if(Command.containsAllRequiredFields(values)) {
                message = new Command(values);
            } else if(CommandResponse.containsAllRequiredFields(values)) {
                message = new CommandResponse(values);
            } else if(SimpleVehicleMessage.containsAllRequiredFields(values)) {
                message = new SimpleVehicleMessage(values);
            } else if(NamedVehicleMessage.containsAllRequiredFields(values)) {
                message = new NamedVehicleMessage(values);
            } else {
                // TODO should we allow generic vehicleMessage through? I think so.
                throw new UnrecognizedMessageTypeException(
                        "Unrecognized combination of entries in values = " +
                        values.toString());
            }
        } catch(InvalidMessageFieldsException e) {
            throw new UnrecognizedMessageTypeException(
                    "Unrecognized combination of entries in values = " +
                    values.toString() + " (should not get here)");
        }

        return message;
    }

    /**
     * @return true if the message has a valid timestamp.
     */
    public boolean isTimestamped() {
        return getTimestamp() != 0;
    }

    /**
     * @return the timestamp of the message in milliseconds since the UNIX
     * epoch.
     */
    public long getTimestamp() {
        return mTimestamp;
    }

    protected Map<String, Object> getValuesMap() {
        return mValues;
    }

    public Object get(String key) {
        return mValues.get(key);
    }

    public boolean contains(String key) {
        return mValues.containsKey(key);
    }

    public void put(String key, Object value) {
        if(key != null && value != null) {
            mValues.put(key, value);
        }
    }

    /**
     * Make the message's timestamp invalid so it won't end up in the
     * serialized version.
     */
    public void untimestamp() {
        mTimestamp = 0;
    }

    private void timestamp() {
        if(!isTimestamped()) {
            mTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("values", getValuesMap())
            .toString();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        final VehicleMessage other = (VehicleMessage) obj;
        return mTimestamp == other.mTimestamp && mValues.equals(other.mValues);
    }

    /* Write the derived class name and timestamp to the parcel, but not any of
     * the values.
     */
    protected void writeMinimalToParcel(Parcel out, int flags) {
        out.writeString(getClass().getName());
        out.writeLong(getTimestamp());
    }

    public void writeToParcel(Parcel out, int flags) {
        writeMinimalToParcel(out, flags);
        out.writeMap(getValuesMap());
    }

    protected void readMinimalFromParcel(Parcel in) {
        // Not reading the derived class name as it is already pulled out of the
        // Parcel by the CREATOR.
        mTimestamp = in.readLong();
    }

    protected void readFromParcel(Parcel in) {
        readMinimalFromParcel(in);
        in.readMap(mValues, null);
    }

    public static final Parcelable.Creator<VehicleMessage> CREATOR =
            new Parcelable.Creator<VehicleMessage>() {
        public VehicleMessage createFromParcel(Parcel in) {
            String messageClassName = in.readString();
            Constructor<? extends VehicleMessage> constructor = null;
            Class<? extends VehicleMessage> messageClass = null;
            try {
                try {
                    messageClass = Class.forName(messageClassName).asSubclass(
                            VehicleMessage.class);
                } catch(ClassNotFoundException e) {
                    throw new UnrecognizedMessageTypeException(
                            "Unrecognized message class: " + messageClassName);
                }

                try {
                    // Must use getDeclaredConstructor because it's a protected
                    // constructor. That's OK since we are the parent class and
                    // should have access, we're not breaking abstraction.
                    constructor = messageClass.getDeclaredConstructor(Parcel.class);
                } catch(NoSuchMethodException e) {
                    throw new UnrecognizedMessageTypeException(messageClass +
                            " doesn't have the expected constructor", e);
                }

                return constructor.newInstance(in);
            } catch(InstantiationException|IllegalAccessException
                    |InvocationTargetException
                    |UnrecognizedMessageTypeException e) {
                Log.e(TAG, "Unable to unparcel a " + messageClass, e);
                return new VehicleMessage();
            }
        }

        public VehicleMessage[] newArray(int size) {
            return new VehicleMessage[size];
        }
    };

    // This must be protected so that we can call it using relfection from this
    // class. Kind of weird, but it works.
    protected VehicleMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }
}
