package com.openxc.messages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;

import com.google.gson.annotations.SerializedName;

public class SimpleModemMessage extends LabeledModemMessage {
    public static final String VALUE_KEY = "modem_value";

    public static final String[] sRequiredFieldsValues = new String[] {
            LABEL_KEY, VALUE_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(VALUE_KEY)
    private Object mValue;

    public SimpleModemMessage(Long timestamp, String label, Object value) {
        super(timestamp, label);
        mValue = value;
    }

    public SimpleModemMessage(String label, Object value) {
        this(null, label, value);
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
        if(!super.equals(obj) || !(obj instanceof SimpleModemMessage)) {
            return false;
        }

        final SimpleModemMessage other = (SimpleModemMessage) obj;
        return mValue.equals(other.mValue);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("label", getLabel())
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

    protected SimpleModemMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected SimpleModemMessage() { }
}
