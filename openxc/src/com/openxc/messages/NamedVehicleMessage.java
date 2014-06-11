package com.openxc.messages;

import java.util.Map;
import java.util.HashMap;

import android.os.Parcel;

import com.google.common.base.Objects;

public class NamedVehicleMessage extends VehicleMessage implements KeyedMessage {
    public static final String NAME_KEY = "name";

    private String mName;

    public NamedVehicleMessage(String name) {
        mName = name;
    }

    public NamedVehicleMessage(Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(values);
        if(!containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }
        mName = (String) getValuesMap().remove(NAME_KEY);
    }

    public NamedVehicleMessage(String name, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(values);
        mName = name;
    }

    public NamedVehicleMessage(Long timestamp, String name, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(timestamp, values);
        mName = name;
    }

    public NamedVehicleMessage(Long timestamp, String name) {
        super(timestamp);
        mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final NamedVehicleMessage other = (NamedVehicleMessage) obj;
        return mName.equals(other.mName);
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(NAME_KEY, getName());
        return new MessageKey(key);
    }

    // TODO oops, can't override a static method so we need to implement this
    // check another way
    protected static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(NAME_KEY);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("name", getName())
            .add("values", getValuesMap())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getName());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mName = in.readString();
    }

    protected NamedVehicleMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected NamedVehicleMessage() { }
}
