package com.openxc.messages;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LabeledModemMessage extends KeyedMessage {
    public static final String LABEL_KEY = "modem_label";

    @SerializedName(LABEL_KEY)
    private String mLabel;

    public static final String[] sRequiredFieldsValues = new String[] {
            LABEL_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    public LabeledModemMessage(String label) {
        mLabel = label;
    }

    public LabeledModemMessage(Long timestamp, String label) {
        super(timestamp);
        mLabel = label;
    }

    public String getLabel() {
        return mLabel;
    }

    @Override
    public int compareTo(VehicleMessage other) {
        LabeledModemMessage otherMessage = (LabeledModemMessage) other;
        int labelComp = getLabel().compareTo(otherMessage.getLabel());
        return labelComp == 0 ? super.compareTo(other) : labelComp;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }

        final LabeledModemMessage other = (LabeledModemMessage) obj;
        return mLabel.equals(other.mLabel);
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(LABEL_KEY, getLabel());
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
            .add("label", getLabel())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getLabel());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mLabel = in.readString();
    }

    protected LabeledModemMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected LabeledModemMessage() { }
}
