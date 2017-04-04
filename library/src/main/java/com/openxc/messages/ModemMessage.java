package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;

import com.google.gson.annotations.SerializedName;

public class ModemMessage extends KeyedMessage {
    public static final String VERSION_KEY = "modem-comm-version";

    @SerializedName(VERSION_KEY)
    private String mVersion;

    public static final String[] sRequiredFieldsValues = new String[] {
            VERSION_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    public ModemMessage(String version) {
        mVersion = version;
    }

    public ModemMessage(Long timestamp, String version) {
        super(timestamp);
        mVersion = version;
    }

    public String getVersion() {
        return mVersion;
    }

    @Override
    public int compareTo(VehicleMessage other) {
        ModemMessage otherMessage = (ModemMessage) other;
        int versionComp = getVersion().compareTo(otherMessage.getVersion());
        return versionComp == 0 ? super.compareTo(other) : versionComp;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }

        final ModemMessage other = (ModemMessage) obj;
        return mVersion.equals(other.mVersion);
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(VERSION_KEY, getVersion());
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
            .add("version", getVersion())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getVersion());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mVersion = in.readString();
    }

    protected ModemMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected ModemMessage() { }
}
