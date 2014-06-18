package com.openxc.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.common.primitives.Bytes;
import com.google.gson.annotations.SerializedName;

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

    public static final String[] sRequiredFieldsValues = new String[] {
            BUS_KEY, ID_KEY, DATA_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    private void setBusId(Map<String, Object> values) {
        mBusId = ((Number) getValuesMap().remove(BUS_KEY)).intValue();
    }

    private void setId(Map<String, Object> values) {
        mId = ((Number) getValuesMap().remove(ID_KEY)).intValue();
    }

    private void setData(Map<String, Object> values) {
        // Kind of a hack, but when we are deserializing from JSON with
        // Gson, we get the data array back as an ArrayList instead of a
        // byte[].
        Object data = getValuesMap().remove(DATA_KEY);
        if(data instanceof ArrayList) {
            setData(Bytes.toArray(((ArrayList<Byte>) data)));
        } else{
            setData((byte[]) data);
        }
    }

    private void setData(byte[] data) {
        if(data != null) {
            System.arraycopy(data, 0, mData, 0, data.length);
        }
    }

    public CanMessage(int canBus, int id, byte[] data) {
        mBusId = canBus;
        mId = id;
        setData(data);
    }

    public CanMessage(int canBus, int id, byte[] data,
            Map<String, Object> extraFields) {
        super(extraFields);
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

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final CanMessage other = (CanMessage) obj;
        return mId == other.mId && mBusId == other.mBusId
                && Arrays.equals(mData, other.mData);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("bus", getBus())
            .add("id", getId())
            .add("data", Arrays.toString(getData()))
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
