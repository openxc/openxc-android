package com.openxc.messages;

import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

/**
 * A diagnostic request message, for example an OBD-II request.
 */
public class DiagnosticRequest extends DiagnosticMessage {

    private static final String MULTIPLE_RESPONSES_KEY = "multiple_responses";
    private static final String FREQUENCY_KEY = "frequency";
    private static final String NAME_KEY = NamedVehicleMessage.NAME_KEY;

    public static final String ADD_ACTION_KEY = "add";
    public static final String CANCEL_ACTION_KEY = "cancel";

    private static final String[] sRequiredFieldsValues = new String[] {
            ID_KEY, BUS_KEY, MODE_KEY };
    private static final Set<String> sRequiredFields = new HashSet<>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(MULTIPLE_RESPONSES_KEY)
    private Boolean mMultipleResponses;

    @SerializedName(FREQUENCY_KEY)
    // Frequency is an optional field, so it is stored as a Double so it can
    // be nulled.
    private Double mFrequency;

    @SerializedName(NAME_KEY)
    private String mName;

    public DiagnosticRequest(int busId, int id, int mode) {
        super(busId, id, mode);
    }

    public DiagnosticRequest(int busId, int id, int mode, int pid) {
        super(busId, id, mode, pid);
    }

    public void setMultipleResponses(boolean multipleResponses) {
        mMultipleResponses = multipleResponses;
    }

    public boolean hasFrequency() {
        return getFrequency() != null;
    }

    public void setFrequency(Double frequency) {
        mFrequency = frequency;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean getMultipleResponses() {
        return mMultipleResponses == null ? false : mMultipleResponses;
    }

    public Double getFrequency() {
        return mFrequency;
    }

    public boolean hasName() {
        return getName() != null;
    }

    public String getName() {
        return mName;
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || !(obj instanceof DiagnosticRequest)) {
            return false;
        }

        final DiagnosticRequest other = (DiagnosticRequest) obj;
        return getMultipleResponses() == other.getMultipleResponses()
                && Objects.equal(mFrequency, other.mFrequency)
                && Objects.equal(mName, other.mName);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("bus", getBusId())
            .add("id", getId())
            .add("mode", getMode())
            .add("pid", getPid())
            .add("payload", Arrays.toString(getPayload()))
            .add("multiple_responses", getMultipleResponses())
            .add("frequency", getFrequency())
            .add("name", getName())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeValue(mMultipleResponses);
        out.writeValue(getFrequency());
        out.writeString(getName());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mMultipleResponses = (Boolean) in.readValue(Boolean.class.getClassLoader());
        mFrequency = (Double) in.readValue(Double.class.getClassLoader());
        mName = in.readString();
    }

    protected DiagnosticRequest(Parcel in) {
        readFromParcel(in);
    }

    protected DiagnosticRequest() { }
}
