package com.openxc.messages;

import android.os.Parcel;

import java.util.Map;
import java.util.HashMap;

import com.google.common.base.Objects;

public class CanMessage extends VehicleMessage implements KeyedMessage {
    public static final String ID_KEY = "id";
    public static final String BUS_KEY = "bus";
    public static final String DATA_KEY = "data";

    private int mBusId;
    private int mId;
    private byte[] mData = new byte[8];

    public CanMessage(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
        if(!containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }

        init((Integer)getValuesMap().remove(BUS_KEY),
                (Integer)getValuesMap().remove(ID_KEY),
                (byte[])getValuesMap().remove(CanMessage.DATA_KEY));
    }

    public CanMessage(int canBus, int id, byte[] data) {
        init(canBus, id, data);
    }

    private void init(int canBus, int id, byte[] data)  {
        mBusId = canBus;
        mId = id;
        System.arraycopy(data, 0, mData, 0, data.length);
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
