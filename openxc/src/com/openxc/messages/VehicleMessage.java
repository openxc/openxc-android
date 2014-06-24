package com.openxc.messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.base.Objects;

public class VehicleMessage implements Parcelable {
    private static final String TAG = "VehicleMessage";
    public static final String TIMESTAMP_KEY = "timestamp";
    private static final long sNoTimestampValue = 0l;

    // We store this as a double in the class so it's simpler to serialize (as a
    // floating point number), even though when you call getTimestamp() we
    // return a long to match the standard Java UNIX time interface.
    private Double mTimestamp;

    // The 'transient' keyword means this will not be serialized - we want to
    // handle it manually so the values aren't wrapped with a "values" object.
    private transient Map<String, Object> mValues = new HashMap<String, Object>();

    public static final Set<String> sRequiredFields = new HashSet<String>();

    public VehicleMessage() { }

    /**
     * @param timestamp timestamp as milliseconds since unix epoch
     */
    public VehicleMessage(Long timestamp) {
        this();
        setTimestamp(timestamp);
    }

    public VehicleMessage(Long timestamp, Map<String, Object> values) {
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

        setValues(values);
        if(contains(TIMESTAMP_KEY)) {
            //TODO not used anywhere
            Double timestampSeconds = (Double) getValuesMap().remove(TIMESTAMP_KEY);
        }
    }

    public void setTimestamp(Long timestamp) {
        if (timestamp == null || timestamp == sNoTimestampValue) {
            untimestamp();
        } else {
            mTimestamp = timestamp / 1000.0;
        }
    }

    /**
     * @return true if the message has a valid timestamp.
     */
    public boolean isTimestamped() {
        return mTimestamp != null;
    }

    /**
     * @return the timestamp of the message in milliseconds since the UNIX
     * epoch.
     */
    public Long getTimestamp() {
        if (!isTimestamped()) {
            return null;
        }
        return Double.valueOf(mTimestamp * 1000.0).longValue();
    }

    protected void setValues(Map<String, Object> values) {
        if(values != null) {
            mValues = new HashMap<String, Object>(values);
        }
    }

    public Map<String, Object> getValuesMap() {
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
        mTimestamp = null;
    }

    public void timestamp() {
        if(!isTimestamped()) {
            mTimestamp = Double.valueOf(System.currentTimeMillis());
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
        return ((mTimestamp == null && other.mTimestamp == null) || 
                mTimestamp == other.mTimestamp) && mValues.equals(other.mValues);
    }

    /* Write the derived class name and timestamp to the parcel, but not any of
     * the values.
     */
    protected void writeMinimalToParcel(Parcel out, int flags) {
        out.writeString(getClass().getName());
        if (isTimestamped()) {
            out.writeLong(getTimestamp());
        } else {
            out.writeLong(sNoTimestampValue);
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        writeMinimalToParcel(out, flags);
        out.writeMap(getValuesMap());
    }

    protected void readMinimalFromParcel(Parcel in) {
        // Not reading the derived class name as it is already pulled out of the
        // Parcel by the CREATOR.
        setTimestamp(in.readLong());
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
