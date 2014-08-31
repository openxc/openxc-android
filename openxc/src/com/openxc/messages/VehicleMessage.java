package com.openxc.messages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class VehicleMessage implements Parcelable, Comparable<VehicleMessage> {
    public interface Listener {
        /* Public: Receive an incoming VehicleMessage.
         */
        public void receive(VehicleMessage message);
    }

    private static final String TAG = "VehicleMessage";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String EXTRAS_KEY = "extras";

    // We store this as a double in the class so it's simpler to serialize (as a
    // floating point number), even though when you call getTimestamp() we
    // return a long to match the standard Java UNIX time interface.
    @SerializedName(TIMESTAMP_KEY)
    private Double mTimestamp;

    @SerializedName(EXTRAS_KEY)
    private Map<String, Object> mExtras;

    public static final Set<String> sRequiredFields = new HashSet<String>();

    public VehicleMessage() { }

    /**
     * @param timestamp timestamp as milliseconds since unix epoch
     */
    public VehicleMessage(Long timestamp) {
        setTimestamp(timestamp);
    }

    public VehicleMessage(Long timestamp, Map<String, Object> extras) {
        this(extras);
        setTimestamp(timestamp);
    }

    /**
     * @param extras A map of any extra data to attach to this message.
     */
    public VehicleMessage(Map<String, Object> extras) {
        setExtras(extras);
    }

    public void setTimestamp(Long timestamp) {
        if(timestamp != null) {
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

    public Date getDate() {
        if (!isTimestamped()) {
            return null;
        }
        return new Date(getTimestamp());
    }

    public void setExtras(Map<String, Object> extras) {
        if(extras != null && !extras.isEmpty()) {
            mExtras = new HashMap<String, Object>(extras);
        }
    }

    public boolean hasExtras() {
        return mExtras != null;
    }

    public Map<String, Object> getExtras() {
        return mExtras;
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
            mTimestamp = Double.valueOf(System.currentTimeMillis() / 1000.0);
        }
    }

    public NamedVehicleMessage asNamedMessage() {
        return (NamedVehicleMessage) this;
    }

    public SimpleVehicleMessage asSimpleMessage() {
        return (SimpleVehicleMessage) this;
    }

    public EventedSimpleVehicleMessage asEventedMessage() {
        return (EventedSimpleVehicleMessage) this;
    }

    public CanMessage asCanMessage() {
        return (CanMessage) this;
    }

    public DiagnosticRequest asDiagnosticRequest() {
        return (DiagnosticRequest) this;
    }

    public DiagnosticResponse asDiagnosticResponse() {
        return (DiagnosticResponse) this;
    }

    public KeyedMessage asKeyedMessage() {
        return (KeyedMessage) this;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int compareTo(VehicleMessage other) {
        return mTimestamp.compareTo(other.mTimestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(this == obj) {
            return true;
        }

        final VehicleMessage other = (VehicleMessage) obj;
        return Objects.equal(mTimestamp, other.mTimestamp) &&
                Objects.equal(mExtras, other.mExtras);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getClass().getName());
        out.writeValue(getTimestamp());
        out.writeValue(getExtras());
    }

    protected void readFromParcel(Parcel in) {
        // Not reading the derived class name as it is already pulled out of the
        // Parcel by the CREATOR.
        setTimestamp((Long)in.readValue(Long.class.getClassLoader()));
        mExtras = (HashMap<String, Object>) in.readValue(
                HashMap.class.getClassLoader());
    }

    public static final Parcelable.Creator<VehicleMessage> CREATOR =
            new Parcelable.Creator<VehicleMessage>() {
        @Override
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

        @Override
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
