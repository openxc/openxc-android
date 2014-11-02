package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.openxc.util.Range;

/**
 * An abstract base class to hold common fields and logic for diagnostic
 * requests and responses.
 *
 * Diagnostic messages are keyed on the bus, message ID, mode and PID (if set).
 */
public abstract class DiagnosticMessage extends KeyedMessage {

    protected static final String ID_KEY = CanMessage.ID_KEY;
    protected static final String BUS_KEY = CanMessage.BUS_KEY;
    protected static final String MODE_KEY = "mode";
    protected static final String PID_KEY = "pid";
    protected static final String PAYLOAD_KEY = "payload";

    protected static final Range<Integer> BUS_RANGE = new Range<>(1, 2);
    protected static final Range<Integer> MODE_RANGE = new Range<>(1, 0xff);
    // Note that this is a limit of the OpenXC Vi firmware at the moment - a
    // diagnostic request can technically have a much larger payload.
    // TODO this is not checked anywhere!
    private static final int MAX_PAYLOAD_LENGTH_IN_BYTES = 7;

    @SerializedName(BUS_KEY)
    private int mBusId;

    @SerializedName(ID_KEY)
    private int mId;

    @SerializedName(MODE_KEY)
    private int mMode;

    @SerializedName(PID_KEY)
    // PID is an optional field, so it is stored as an Integer so it can be
    // nulled.
    private Integer mPid;

    @SerializedName(PAYLOAD_KEY)
    private byte[] mPayload;

    public DiagnosticMessage(int busId, int id, int mode) {
        mBusId = busId;
        mId = id;
        mMode = mode;
    }

    public DiagnosticMessage(int busId, int id, int mode, int pid) {
        this(busId, id, mode);
        mPid = pid;
    }

    public boolean hasPid() {
        return mPid != null;
    }

    public int getBusId() {
        return mBusId;
    }

    public int getId() {
        return mId;
    }

    public int getMode() {
        return mMode;
    }

    public Integer getPid() {
        return mPid;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public void setPid(int pid) {
        mPid = pid;
    }

    public boolean hasPayload() {
        return mPayload != null;
    }

    public void setPayload(byte[] payload) {
        if(payload != null) {
            mPayload = new byte[payload.length];
            System.arraycopy(payload, 0, mPayload, 0, payload.length);
        } else {
            mPayload = null;
        }
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(CanMessage.BUS_KEY, getBusId());
            key.put(CanMessage.ID_KEY, getId());
            key.put(MODE_KEY, getMode());
            key.put(PID_KEY, getPid());
            setKey(new MessageKey(key));
        }
        return super.getKey();
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }

        final DiagnosticMessage other = (DiagnosticMessage) obj;
        return mBusId == other.mBusId
                && mId == other.mId
                && mMode == other.mMode
                && mPid == other.mPid
                && Arrays.equals(mPayload, other.mPayload);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(getBusId());
        out.writeInt(getId());
        out.writeInt(getMode());
        out.writeValue(getPid());
        if(getPayload() != null) {
            out.writeInt(getPayload().length);
            out.writeByteArray(getPayload());
        } else {
            out.writeInt(0);
        }
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mBusId = in.readInt();
        mId = in.readInt();
        mMode = in.readInt();
        mPid = (Integer) in.readValue(Integer.class.getClassLoader());

        int payloadSize = in.readInt();
        if(payloadSize > 0) {
            mPayload = new byte[payloadSize];
            in.readByteArray(mPayload);
        }
    }

    protected DiagnosticMessage() { }
}
