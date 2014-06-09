package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class VehicleMessage implements Parcelable {
    public static final String TIMESTAMP_KEY = "timestamp";

    private long mTimestamp;
    private Map<String, Object> mValues = new HashMap<String, Object>();

    public VehicleMessage(Long timestamp, Map<String, Object> values) {
        if(timestamp == null) {
            if(values != null && values.containsKey(TIMESTAMP_KEY)) {
                mTimestamp = (Long) values.remove(TIMESTAMP_KEY);
            } else {
                timestamp();
            }
        } else {
            mTimestamp = timestamp;
        }

        if(values != null) {
            mValues = new HashMap<String, Object>(values);
        }
    }

    public VehicleMessage(Map<String, Object> values) {
        this(null, values);
    }

    public static VehicleMessage buildSubtype(Map<String, Object> values)
            throws UnrecognizedMeasurementTypeException {
        // Must check from most specific to least
        VehicleMessage message;
        // TODO could clean this up with reflection since they all now have the
        // same constructor
        if(CanMessage.matchesKeys(values)) {
            message = new CanMessage(values);
        } else if(DiagnosticResponse.matchesKeys(values)) {
            message = new DiagnosticResponse(values);
        } else if(DiagnosticRequest.matchesKeys(values)) {
            message = new DiagnosticRequest(values);
        } else if(CommandMessage.matchesKeys(values)) {
            message = new CommandMessage(values);
        } else if(CommandResponse.matchesKeys(values)) {
            message = new CommandResponse(values);
        } else if(SimpleVehicleMessage.matchesKeys(values)) {
            message = new SimpleVehicleMessage(values);
        } else if(NamedVehicleMessage.matchesKeys(values)) {
            message = new NamedVehicleMessage(values);
        } else {
            throw new UnrecognizedMeasurementTypeException("Unrecognized combination of entries in values = " + values.toString());
        }

        return message;
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

    /**
     * Make the measurement's timestamp invalid so it won't end up in the
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

    public static final Parcelable.Creator<VehicleMessage> CREATOR =
            new Parcelable.Creator<VehicleMessage>() {
        public VehicleMessage createFromParcel(Parcel in) {
            String messageClassName = in.readString();
            try {
                if(messageClassName.equals(VehicleMessage.class.getName())) {
                    return new VehicleMessage(in);
                } else if(messageClassName.equals(NamedVehicleMessage.class.getName())) {
                    return new NamedVehicleMessage(in);
                } else if(messageClassName.equals(SimpleVehicleMessage.class.getName())) {
                    return new SimpleVehicleMessage(in);
                } else {
                    throw new UnrecognizedMeasurementTypeException(
                            "Unrecognized message class: " + messageClassName);
                }
            } catch(UnrecognizedMeasurementTypeException e) {
                return new VehicleMessage();
            }
        }

        public VehicleMessage[] newArray(int size) {
            return new VehicleMessage[size];
        }
    };

    protected static boolean matchesKeys(Map<String, Object> map) {
        return true;
    }

    private VehicleMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        readFromParcel(in);
    }

    protected VehicleMessage() { }
}
