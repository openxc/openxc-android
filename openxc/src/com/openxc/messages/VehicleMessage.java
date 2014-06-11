package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

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

    public VehicleMessage(Long timestamp) {
        if(timestamp != null) {
            mTimestamp = timestamp;
        } else {
            // TODO when should things get timestampped?
            // timestamp();
            mTimestamp = 0;
        }
    }

    public VehicleMessage(Long timestamp, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        this(timestamp);
        if(values != null) {
            mValues = new HashMap<String, Object>(values);
        }

        if(contains(TIMESTAMP_KEY)) {
            mTimestamp = (Long) getValuesMap().remove(TIMESTAMP_KEY);
        }

        if(mTimestamp == 0) {
            timestamp();
        }
    }

    public VehicleMessage(Map<String, Object> values)
            throws InvalidMessageFieldsException {
        this(null, values);
    }

    public static VehicleMessage buildSubtype(Map<String, Object> values)
            throws UnrecognizedMessageTypeException {
        // Must check from most specific to least
        VehicleMessage message = new VehicleMessage();
        // TODO could clean this up with reflection since they all now have the
        // same constructor
        try {
            if(CanMessage.containsRequiredFields(values)) {
                message = new CanMessage(values);
            } else if(DiagnosticResponse.containsRequiredFields(values)) {
                message = new DiagnosticResponse(values);
            } else if(DiagnosticRequest.containsRequiredFields(values)) {
                message = new DiagnosticRequest(values);
            } else if(CommandMessage.containsRequiredFields(values)) {
                message = new CommandMessage(values);
            } else if(CommandResponse.containsRequiredFields(values)) {
                message = new CommandResponse(values);
            } else if(SimpleVehicleMessage.containsRequiredFields(values)) {
                message = new SimpleVehicleMessage(values);
            } else if(NamedVehicleMessage.containsRequiredFields(values)) {
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
            try {
                // TODO need to handle unparceling other types
                if(messageClassName.equals(VehicleMessage.class.getName())) {
                    return new VehicleMessage(in);
                } else if(messageClassName.equals(NamedVehicleMessage.class.getName())) {
                    return new NamedVehicleMessage(in);
                } else if(messageClassName.equals(SimpleVehicleMessage.class.getName())) {
                    return new SimpleVehicleMessage(in);
                } else if(messageClassName.equals(CommandResponse.class.getName())) {
                    return new CommandResponse(in);
                } else if(messageClassName.equals(CommandMessage.class.getName())) {
                    return new CommandMessage(in);
                } else if(messageClassName.equals(CanMessage.class.getName())) {
                    return new CanMessage(in);
                } else if(messageClassName.equals(DiagnosticRequest.class.getName())) {
                    return new DiagnosticRequest(in);
                } else if(messageClassName.equals(DiagnosticResponse.class.getName())) {
                    return new DiagnosticResponse(in);
                } else {
                    throw new UnrecognizedMessageTypeException(
                            "Unrecognized message class: " + messageClassName);
                }
            } catch(UnrecognizedMessageTypeException e) {
                return new VehicleMessage();
            }
        }

        public VehicleMessage[] newArray(int size) {
            return new VehicleMessage[size];
        }
    };

    private VehicleMessage(Parcel in) throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

}
