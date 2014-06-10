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

    public DiagnosticRequest(Map<String, Object> values) {
        super(values);
        if (values != null) {
            if (values.containsKey(MULTIPLE_RESPONSES_KEY)) {
                mMultipleResponses = (boolean) values.get(MULTIPLE_RESPONSES_KEY);
            }

            if (values.containsKey(FACTOR_KEY)) {
                mFactor = (float) values.get(FACTOR_KEY);
            }

            if (values.containsKey(OFFSET_KEY)) {
                mOffset = (float) values.get(OFFSET_KEY);
            }

            if (values.containsKey(FREQUENCY_KEY)) {
                mFrequency = (float) values.get(FREQUENCY_KEY);
            }

            if (values.containsKey(NamedVehicleMessage.NAME_KEY)) {
                mName = (String) values.get(NamedVehicleMessage.NAME_KEY);
            }
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
        if (obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticRequest other = (DiagnosticRequest) obj;
        return mMultipleResponses == other.mMultipleResponses
                && mFactor == other.mFactor
                && mOffset == other.mOffset
                && mFrequency == other.mFrequency
                && mName.equals(other.mName);
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

}
