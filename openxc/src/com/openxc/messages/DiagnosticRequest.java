package com.openxc.messages;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.google.common.primitives.Bytes;
import com.google.common.base.Objects;

import com.openxc.util.Range;

public class DiagnosticRequest extends VehicleMessage implements KeyedMessage {

    public static final String ID_KEY = CanMessage.ID_KEY;
    public static final String BUS_KEY = CanMessage.BUS_KEY;
    public static final String MODE_KEY = "mode";
    public static final String PID_KEY = "pid";
    public static final String PAYLOAD_KEY = "payload";
    public static final String MULTIPLE_RESPONSES_KEY = "multiple_responses";
    public static final String FREQUENCY_KEY = "frequency";
    public static final String NAME_KEY = NamedVehicleMessage.NAME_KEY;

    public static final Range<Integer> BUS_RANGE = new Range<>(1, 2);
    public static final Range<Integer> MODE_RANGE = new Range<>(1, 0xff);
    // Note that this is a limit of the OpenXC Vi firmware at the moment - a
    // diagnostic request can tehcnically have a much larger payload.
    public static final int MAX_PAYLOAD_LENGTH_IN_BYTES = 7;

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

    @SerializedName(MULTIPLE_RESPONSES_KEY)
    private boolean mMultipleResponses = false;

    @SerializedName(FREQUENCY_KEY)
    // Frequency is an optional field, so it is stored as an Integer so it can
    // be nulled.
    private Double mFrequency;

    @SerializedName(NAME_KEY)
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
            byte[] payload, boolean multipleResponses, double frequency,
            String name) {
        this(busId, id, mode, pid, payload);
        mMultipleResponses = multipleResponses;
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

        // Because of the way we are deserializing from JSON, we get all numbers
        // as Double.
        mBusId = ((Number) getValuesMap().remove(CanMessage.BUS_KEY)).intValue();
        mId = ((Number) getValuesMap().remove(CanMessage.ID_KEY)).intValue();
        mMode = ((Number) getValuesMap().remove(MODE_KEY)).intValue();

        if(contains(PID_KEY)) {
            mPid = ((Number) getValuesMap().remove(PID_KEY)).intValue();
        }

        if(contains(PAYLOAD_KEY)) {
            // Same weird workaround as in CanMessage
            Object payload = getValuesMap().remove(PAYLOAD_KEY);
            if(payload instanceof ArrayList) {
                setPayload(Bytes.toArray(((ArrayList<Byte>) payload)));
            } else{
                setPayload((byte[]) payload);
            }
        }

        if(contains(MULTIPLE_RESPONSES_KEY)) {
            mMultipleResponses = (boolean) getValuesMap().remove(MULTIPLE_RESPONSES_KEY);
        }

        if(contains(FREQUENCY_KEY)) {
            mFrequency = (double) getValuesMap().remove(FREQUENCY_KEY);
        }

        if(contains(NAME_KEY)) {
            mName = (String) getValuesMap().remove(NAME_KEY);
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

    public Integer getPid() {
        return mPid;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    void setPayload(byte[] payload) {
        if(payload != null) {
            mPayload = new byte[payload.length];
            System.arraycopy(payload, 0, mPayload, 0, payload.length);
        }
    }

    public boolean getMultipleResponses() {
        return mMultipleResponses;
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
                && Arrays.equals(mPayload, other.mPayload)
                && mMultipleResponses == other.mMultipleResponses
                && mFrequency == other.mFrequency
                && ((mName == null && other.mName == null)
                    || mName.equals(other.mName));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("bus", getBusId())
            .add("id", getId())
            .add("mode", getMode())
            .add("pid", getPid())
            .add("payload", Arrays.toString(getPayload()))
            .add("multiple_responses", getMultipleResponses())
            .add("frequency", getFrequency())
            .add("name", getName())
            .add("values", getValuesMap())
            .toString();
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
        out.writeByte((byte) (getMultipleResponses() ? 1 : 0));
        out.writeValue(getFrequency());
        out.writeString(getName());
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

        mMultipleResponses = in.readByte() != 0;
        mFrequency = (Double) in.readValue(Double.class.getClassLoader());
        mName = in.readString();
    }

    protected DiagnosticRequest(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected DiagnosticRequest() { }
}
