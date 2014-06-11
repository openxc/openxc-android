package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.google.common.base.Objects;

public class DiagnosticRequest extends DiagnosticMessage {

    public static final String MULTIPLE_RESPONSES_KEY = "multiple_responses";
    public static final String FACTOR_KEY = "factor";
    public static final String OFFSET_KEY = "offset";
    public static final String FREQUENCY_KEY = "frequency";

    private boolean mMultipleResponses = false;
    private float mFactor = 1f;
    private float mOffset = 0f;
    private float mFrequency = 0f;
    private String mName;

    public DiagnosticRequest(int busId, int id, int mode, byte[] payload) {
        super(busId, id, mode, payload);
    }

    public DiagnosticRequest(int busId, int id, int mode, int pid,
            byte[] payload) {
        super(busId, id, mode, pid, payload);
    }

    public DiagnosticRequest(int busId, int id, int mode, int pid,
            byte[] payload, boolean multipleResponses, float factor,
            float offset, float frequency, String name) {
        super(busId, id, mode, pid, payload);
        mMultipleResponses = multipleResponses;
        mFactor = factor;
        mOffset = offset;
        mFrequency = frequency;
        mName = name;
    }

    public DiagnosticRequest(Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(values);
        if(contains(MULTIPLE_RESPONSES_KEY)) {
            mMultipleResponses = (boolean) getValuesMap().remove(MULTIPLE_RESPONSES_KEY);
        }

        if(contains(FACTOR_KEY)) {
            mFactor = (float) getValuesMap().remove(FACTOR_KEY);
        }

        if(contains(OFFSET_KEY)) {
            mOffset = (float) getValuesMap().remove(OFFSET_KEY);
        }

        if(contains(FREQUENCY_KEY)) {
            mFrequency = (float) getValuesMap().remove(FREQUENCY_KEY);
        }

        if(contains(NamedVehicleMessage.NAME_KEY)) {
            mName = (String) getValuesMap().remove(NamedVehicleMessage.NAME_KEY);
        }
    }

    public boolean getMultipleResponses() {
        return mMultipleResponses;
    }

    public float getFactor() {
        return mFactor;
    }

    public float getOffset() {
        return mOffset;
    }

    public float getFrequency() {
        return mFrequency;
    }

    public String getName() {
        return mName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticRequest other = (DiagnosticRequest) obj;
        return mMultipleResponses == other.mMultipleResponses
                && mFactor == other.mFactor
                && mOffset == other.mOffset
                && mFrequency == other.mFrequency
                && ((mName == null && other.mName == null)
                    || mName.equals(other.mName));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("id", getId())
            .add("mode", getMode())
            .add("pid", getPid())
            .add("payload", getPayload())
            .add("multiple_responses", getMultipleResponses())
            .add("factor", getFactor())
            .add("offset", getOffset())
            .add("frequency", getFrequency())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (getMultipleResponses() ? 1 : 0));
        out.writeFloat(getFactor());
        out.writeFloat(getOffset());
        out.writeFloat(getFrequency());
        out.writeString(getName());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mMultipleResponses = in.readByte() != 0;
        mFactor = in.readFloat();
        mOffset = in.readFloat();
        mFrequency = in.readFloat();
        mName = in.readString();
    }

    protected DiagnosticRequest(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected DiagnosticRequest() { }
}
