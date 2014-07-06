package com.openxc.messages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class SimpleVehicleMessage extends NamedVehicleMessage {
    public static final String VALUE_KEY = "value";

    public static final String[] sRequiredFieldsValues = new String[] {
            NAME_KEY, VALUE_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(VALUE_KEY)
    private Object mValue;

    public SimpleVehicleMessage(Long timestamp, String name, Object value) {
        super(timestamp, name);
        mValue = value;
    }

    public SimpleVehicleMessage(String name, Object value) {
        this(null, name, value);
    }

    public Object getValue() {
        return mValue;
    }

    public Number getValueAsNumber() {
        return (Number) mValue;
    }

    public String getValueAsString() {
        return (String) mValue;
    }

    public Boolean getValueAsBoolean() {
        return (Boolean) mValue;
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final SimpleVehicleMessage other = (SimpleVehicleMessage) obj;
        return mValue.equals(other.mValue);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("name", getName())
            .add("value", getValue())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeValue(getValue());
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mValue = in.readValue(null);
    }

    protected SimpleVehicleMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected SimpleVehicleMessage() { }
}
