package com.openxc.messages;

import com.google.common.base.Objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

/**
 * The VehicleMessage is the most basic, root form of data going back and forth
 * between the OpenXC library and a vehicle interface. The VI's data stream is
 * message based, where each message is a subtype of a VehicleMessage.
 *
 * This base class implements Parcelable, so it can be sent over an AIDL
 * interface, i.e. between the singleton VehicleService and the VehicleManager
 * instance in each app's process.
 */
public class VehicleMessage implements Parcelable, Comparable<VehicleMessage> {
    private static final String TAG = "VehicleMessage";

    public interface Listener {
        /* Public: Receive an incoming VehicleMessage.
         */
        public void receive(VehicleMessage message);
    }

    /**
     * The field names for the timsetamp and extra data, defined in the OpenXC
     * Message Format specification.
     */
    private static final String TIMESTAMP_KEY = "timestamp";
    public static final String EXTRAS_KEY = "extras";

    @SerializedName(TIMESTAMP_KEY)
    // This is a bit of hack to be able to serialize timestamps to JSON as
    // seconds with floating point precision, but represent them internally as
    // longs (milliseconds) for performance.
    private Double mTimestampSeconds;

    private transient Long mTimestamp;

    @SerializedName(EXTRAS_KEY)
    private Map<String, Object> mExtras;

    public VehicleMessage() { }

    /**
     * Construct a new empty VehicleMessage.
     *
     * @param timestamp timestamp as milliseconds since unix epoch
     */
    public VehicleMessage(Long timestamp) {
        setTimestamp(timestamp);
    }

    /**
     * Construct a new VehicleMessage with the given extra data and an
     * overridden timestamp.
     */
    public VehicleMessage(Long timestamp, Map<String, Object> extras) {
        this(extras);
        setTimestamp(timestamp);
    }

    /**
     * Construct a new VehicleMessage with the given extra data.
     *
     * The extras field can hold any arbitrary key/value pairs.
     *
     * @param extras A map of any extra data to attach to this message.
     */
    public VehicleMessage(Map<String, Object> extras) {
        setExtras(extras);
    }

    /**
     * Override the timestamp of the message.
     *
     * @param timestamp the timestamp to set for this message.
     */
    public void setTimestamp(Long timestamp) {
        if(timestamp != null) {
            mTimestamp = timestamp;
            mTimestampSeconds = timestamp / 1000.0;
        }
    }

    /**
     * @return true if the message has a valid timestamp.
     */
    public boolean isTimestamped() {
        return getTimestamp() != null;
    }

    /**
     * @return the timestamp of the message in milliseconds since the UNIX
     * epoch.
     */
    public Long getTimestamp() {
        if(mTimestampSeconds != null) {
            mTimestamp = new Double(mTimestampSeconds * 1000).longValue();
        }
        return mTimestamp;
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
        mTimestampSeconds = null;
    }

    public void timestamp() {
        if(!isTimestamped()) {
            mTimestamp = System.currentTimeMillis();
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

    public CommandResponse asCommandResponse() {
        return (CommandResponse) this;
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
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int compareTo(VehicleMessage other) {
        return getTimestamp().compareTo(other.getTimestamp());
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
        return Objects.equal(getTimestamp(), other.getTimestamp()) &&
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
