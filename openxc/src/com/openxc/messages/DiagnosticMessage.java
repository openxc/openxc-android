package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.util.Range;

public abstract class DiagnosticMessage extends VehicleMessage {

    public static final String PID_KEY = "pid";
    public static final String PAYLOAD_KEY = "payload";
    // TODO can there be more busses?
    public static final Range<Integer> BUS_RANGE = new Range<>(1, 2);
    public static final Range<Integer> MODE_RANGE = new Range<>(1, 15);
    public static final int MAX_PAYLOAD_LENGTH_IN_BYTES = 7;
    public static final int MAX_PAYLOAD_LENGTH_IN_CHARS = MAX_PAYLOAD_LENGTH_IN_BYTES * 2;

    protected int mCanBus;
    protected int mId;
    protected int mMode;
    protected int mPid;
    protected byte[] mPayload;

    protected DiagnosticMessage(Map<String, Object> values) {
        super(values);
        if (values != null) {
            if (values.containsKey(BUS_KEY)) {
                mCanBus = (int) values.get(VehicleMessage.BUS_KEY);
            }
            if (values.containsKey(ID_KEY)) {
                mId = (int) values.get(VehicleMessage.ID_KEY);
            }
            if (values.containsKey(MODE_KEY)) {
                mMode = (int) values.get(VehicleMessage.MODE_KEY);
            }
            if (values.containsKey(PID_KEY)) {
                mPid = (int) values.get(PID_KEY);
            }
            if (values.containsKey(PAYLOAD_KEY)) {
                // TODO what's the right way to convert this?
                // https://github.com/openxc/openxc-message-format
                // says "bytes [...] as a hexidecimal number in a string"
                mPayload = ((String) values.get(PAYLOAD_KEY)).getBytes();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticMessage other = (DiagnosticMessage) obj;
        return super.equals(other) && (mCanBus == other.mCanBus)
                && (mId == other.mId) && (mMode == other.mMode)
                && (mPid == other.mPid) && (mPayload.equals(other.mPayload));
    }

    public int getCanBus() {
        return mCanBus;
    }

    public int getId() {
        return mId;
    }

    public int getMode() {
        return mMode;
    }

    public int getPid() {
        return mPid;
    }

    public byte[] getPayload() {
        return mPayload;
    }
    
    /**
     * @return string concatenation of the bus, id, mode, and pid
     */
    public String getDiagnosticIdentifier() {
        return String.valueOf(getCanBus()) + String.valueOf(getId()) + String.valueOf(getMode()) + String.valueOf(getPid());
    }

    // TODO this is a guess, not 100% sure how this parcel stuff fits in
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(getCanBus());
        out.writeInt(getId());
        out.writeInt(getMode());
        out.writeInt(getPid());
        out.writeByteArray(getPayload());
    }

    // TODO this is a guess, not 100% sure how this parcel stuff fits in
    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCanBus = in.readInt();
        mId = in.readInt();
        mMode = in.readInt();
        mPid = in.readInt();
        in.readByteArray(mPayload);
    }

}
