package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class VehicleMessage implements Parcelable {
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String NAME_KEY = "name";
    public static final String ID_KEY = "id";
    public static final String BUS_KEY = "bus";
    public static final String MODE_KEY = "mode";

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
        VehicleMessage message;
        if (values.containsKey(BUS_KEY) && values.containsKey(ID_KEY) && values.containsKey(CanMessage.DATA_KEY)) {
                message = new CanMessage((int)values.get(BUS_KEY), (int)values.get(ID_KEY), (byte[])values.get(CanMessage.DATA_KEY));
        } else if (values.containsKey(BUS_KEY) && values.containsKey(ID_KEY) && values.containsKey(MODE_KEY)) {
            if (values.containsKey(DiagnosticResponse.SUCCESS_KEY)) {
                message = new DiagnosticResponse(values);
            } else {
                message = new DiagnosticRequest(values);
            }
        } else if (values.containsKey(CommandMessage.COMMAND_KEY)) {
            message = new CommandMessage((String)values.get(CommandMessage.COMMAND_KEY), values);
        } else if (values.containsKey(CommandResponse.COMMAND_RESPONSE_KEY) && values.containsKey(CommandResponse.MESSAGE_KEY)) {
            message = new CommandResponse((String)values.get(CommandResponse.COMMAND_RESPONSE_KEY), values);
        }
        //this check must be done last (or at least after checking if it's a DiagnosticRequest because that
        //might have a name field too)
        else if(values.containsKey(NamedVehicleMessage.NAME_KEY)) {
            if(values.containsKey(SimpleVehicleMessage.VALUE_KEY)) {
                message = new SimpleVehicleMessage(values);
            } else {
                message = new NamedVehicleMessage(values);
            }
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
                    // TODO branches for other types
                    return new VehicleMessage();
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


    private VehicleMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    protected VehicleMessage() { }
}
