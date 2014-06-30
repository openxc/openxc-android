package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class CanMessage extends KeyedMessage {
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

    public CanMessage(int canBus, int id, byte[] data) {
        mBusId = canBus;
        mId = id;
        setPayload(data);
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

    private void setPayload(byte[] data) {
        if(data != null) {
            System.arraycopy(data, 0, mData, 0, data.length);
        }
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
        super.writeToParcel(out, flags);
        out.writeInt(getBus());
        out.writeInt(getId());
        out.writeByteArray(getData());
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mBusId = in.readInt();
        mId = in.readInt();
        in.readByteArray(mData);
    }

    protected CanMessage(Parcel in) throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }
}
