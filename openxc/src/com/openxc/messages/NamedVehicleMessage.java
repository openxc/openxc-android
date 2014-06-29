package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class NamedVehicleMessage extends KeyedMessage {
    public static final String NAME_KEY = "name";

    @SerializedName(NAME_KEY)
    private String mName;

    public static final String[] sRequiredFieldsValues = new String[] {
            NAME_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    public NamedVehicleMessage(String name) {
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

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("name", getName())
            .add("extras", getExtras())
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
