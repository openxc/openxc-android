package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

/**
 * A NamedVehicleMessage is a VehicleMessage with a name field.
 *
 * Named messages are keyed on the name.
 */
public class NamedVehicleMessage extends KeyedMessage {
    protected static final String NAME_KEY = "name";

    @SerializedName(NAME_KEY)
    private String mName;

    private static final String[] sRequiredFieldsValues = new String[] {
            NAME_KEY };
    private static final Set<String> sRequiredFields = new HashSet<>(
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
    public int compareTo(VehicleMessage other) {
        NamedVehicleMessage otherMessage = (NamedVehicleMessage) other;
        int nameComp = getName().compareTo(otherMessage.getName());
        return nameComp == 0 ? super.compareTo(other) : nameComp;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }

        final NamedVehicleMessage other = (NamedVehicleMessage) obj;
        return mName.equals(other.mName);
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(NAME_KEY, getName());
            setKey(new MessageKey(key));
        }
        return super.getKey();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
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

    protected NamedVehicleMessage(Parcel in) {
        readFromParcel(in);
    }

    protected NamedVehicleMessage() { }
}
