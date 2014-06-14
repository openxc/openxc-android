package com.openxc.messages;

import java.util.Map;
import java.util.HashMap;

import android.os.Parcel;

import com.google.common.base.Objects;

import com.openxc.util.Range;

public class DiagnosticRequest extends VehicleMessage implements KeyedMessage {

    public static final String ID_KEY = CanMessage.ID_KEY;
    public static final String BUS_KEY = CanMessage.BUS_KEY;
    public static final String MODE_KEY = "mode";
    public static final String PID_KEY = "pid";
    public static final String PAYLOAD_KEY = "payload";
    public static final String MULTIPLE_RESPONSES_KEY = "multiple_responses";
    public static final String FACTOR_KEY = "factor";
    public static final String OFFSET_KEY = "offset";
    public static final String FREQUENCY_KEY = "frequency";

    // TODO can there be more buses?
    public static final Range<Integer> BUS_RANGE = new Range<>(1, 2);
    public static final Range<Integer> MODE_RANGE = new Range<>(1, 15);
    public static final int MAX_PAYLOAD_LENGTH_IN_BYTES = 7;

    private int mBusId;
    private int mId;
    private int mMode;
    private Integer mPid;
    private byte[] mPayload;
    private boolean mMultipleResponses = false;
    private Double mFactor = 1.0;
    private Double mOffset = 0.0;
    private Double mFrequency;
    private String mName;

    public DiagnosticRequest(int busId, int id, int mode) {
        mBusId = busId;
        mId = id;
        mMode = mode;
    }

    public DiagnosticRequest(int busId, int id, int mode, int pid) {
        this(busId, id, mode);
        mPid = pid;
    }

    public DiagnosticRequest(int busId, int id, int mode, byte[] payload) {
        this(busId, id, mode);
        setPayload(payload);
    }

    public DiagnosticRequest(int busId, int id, int mode, int pid,
            byte[] payload) {
        this(busId, id, mode, payload);
        mPid = pid;
    }

    public DiagnosticRequest(int busId, int id, int mode, int pid,
            byte[] payload, boolean multipleResponses, double factor,
            double offset, double frequency, String name) {
        this(busId, id, mode, pid, payload);
        mMultipleResponses = multipleResponses;
        mFactor = factor;
        mOffset = offset;
        mFrequency = frequency;
        mName = name;
    }

    public DiagnosticRequest(Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(values);
        if(!containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }

        mBusId = (Integer) getValuesMap().remove(CanMessage.BUS_KEY);
        mId = (Integer) getValuesMap().remove(CanMessage.ID_KEY);
        mMode = (Integer) getValuesMap().remove(MODE_KEY);

        if(contains(PID_KEY)) {
            mPid = (Integer) getValuesMap().remove(PID_KEY);
        }

        if(contains(PAYLOAD_KEY)) {
            setPayload((byte[]) getValuesMap().remove(PAYLOAD_KEY));
        }

        if(contains(MULTIPLE_RESPONSES_KEY)) {
            mMultipleResponses = (boolean) getValuesMap().remove(MULTIPLE_RESPONSES_KEY);
        }

        if(contains(FACTOR_KEY)) {
            mFactor = (double) getValuesMap().remove(FACTOR_KEY);
        }

        if(contains(OFFSET_KEY)) {
            mOffset = (double) getValuesMap().remove(OFFSET_KEY);
        }

        if(contains(FREQUENCY_KEY)) {
            mFrequency = (double) getValuesMap().remove(FREQUENCY_KEY);
        }

        if(contains(NamedVehicleMessage.NAME_KEY)) {
            mName = (String) getValuesMap().remove(NamedVehicleMessage.NAME_KEY);
        }
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

    public int getPid() {
        return mPid;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    void setPayload(byte[] payload) {
        if(payload != null) {
            mPayload = new byte[MAX_PAYLOAD_LENGTH_IN_BYTES];
            System.arraycopy(payload, 0, mPayload, 0,
                    Math.min(payload.length, mPayload.length));
        }
    }

    public boolean getMultipleResponses() {
        return mMultipleResponses;
    }

    public double getFactor() {
        return mFactor;
    }

    public double getOffset() {
        return mOffset;
    }

    public Double getFrequency() {
        return mFrequency;
    }

    public String getName() {
        return mName;
    }

    protected static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(BUS_KEY) && map.containsKey(ID_KEY)
                && map.containsKey(MODE_KEY);
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(CanMessage.BUS_KEY, getBusId());
        key.put(CanMessage.ID_KEY, getId());
        key.put(MODE_KEY, getMode());
        key.put(PID_KEY, getPid());
        return new MessageKey(key);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticRequest other = (DiagnosticRequest) obj;
        return mBusId == other.mBusId
                && mId == other.mId
                && mMode == other.mMode
                && mPid == other.mPid
                // TODO
                // && mPayload.equals(other.mPayload);
                && mMultipleResponses == other.mMultipleResponses
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
        out.writeInt(getBusId());
        out.writeInt(getId());
        out.writeInt(getMode());
        out.writeInt(getPid());
        // TODO
        // out.writeByteArray(getPayload());
        out.writeByte((byte) (getMultipleResponses() ? 1 : 0));
        out.writeDouble(getFactor());
        out.writeDouble(getOffset());
        out.writeValue(getFrequency());
        out.writeString(getName());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mBusId = in.readInt();
        mId = in.readInt();
        mMode = in.readInt();
        mPid = in.readInt();
        // TODO
        // in.readByteArray(mPayload);
        mMultipleResponses = in.readByte() != 0;
        mFactor = in.readDouble();
        mOffset = in.readDouble();
        mFrequency = (Double) in.readValue(Double.class.getClassLoader());
        mName = in.readString();
    }

    protected DiagnosticRequest(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected DiagnosticRequest() { }
}
