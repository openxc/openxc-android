package com.openxc.messages;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.annotations.SerializedName;
import com.google.common.primitives.Bytes;
import com.google.common.base.Objects;

public class CanMessage extends VehicleMessage implements KeyedMessage {
    public static final String ID_KEY = "id";
    public static final String BUS_KEY = "bus";
    public static final String DATA_KEY = "data";

    @SerializedName(BUS_KEY)
    private int mBusId;

    @SerializedName(ID_KEY)
    private int mId;

    @SerializedName(DATA_KEY)
    private byte[] mData = new byte[8];

    public CanMessage(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
        if(!containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }

        setBusId(getValuesMap());
        setId(getValuesMap());
        setData(getValuesMap());
    }

    private void setBusId(Map<String, Object> values) {
        mBusId = ((Number) getValuesMap().remove(BUS_KEY)).intValue();
    }

    private void setId(Map<String, Object> values) {
        mId = ((Number) getValuesMap().remove(ID_KEY)).intValue();
    }

    private void setData(Map<String, Object> values) {
        // Kind of a hack, but when we are deserializing from JSON with
        // Gson, we get the payload array back as an ArrayList instead of a
        // byte[].
        Object data = getValuesMap().remove(DATA_KEY);
        if(data instanceof ArrayList) {
            setData(Bytes.toArray(((ArrayList<Byte>) data)));
        } else{
            setData((byte[]) data);
        }
    }

    private void setData(byte[] data) {
        System.arraycopy(data, 0, mData, 0, data.length);
    }

    public CanMessage(int canBus, int id, byte[] data) {
        mBusId = canBus;
        mId = id;
        setData(data);
    }

    public int getBus() {
        return mBusId;
    }
    public int getId() {
        return mId;
    }

    public byte[] getData() {
        return mData;
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(BUS_KEY, getBus());
        key.put(ID_KEY, getId());
        return new MessageKey(key);
    }

    protected static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(BUS_KEY) && map.containsKey(ID_KEY)
                && map.containsKey(DATA_KEY);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final CanMessage other = (CanMessage) obj;
        return mId == other.mId && mBusId == other.mBusId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("bus", getBus())
            .add("id", getId())
            .add("data", getData())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        writeMinimalToParcel(out, flags);
        out.writeInt(getBus());
        out.writeInt(getId());
        out.writeByteArray(getData());
    }

    public void readFromParcel(Parcel in) {
        readMinimalFromParcel(in);
        mBusId = in.readInt();
        mId = in.readInt();
        in.readByteArray(mData);
    }

    protected CanMessage(Parcel in) throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    private CanMessage() { }
}
