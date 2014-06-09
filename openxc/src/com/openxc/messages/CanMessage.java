package com.openxc.messages;

import android.os.Parcel;

import java.util.Map;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class CanMessage extends VehicleMessage {
    public static final String ID_KEY = "id";
    public static final String BUS_KEY = "bus";
    public static final String DATA_KEY = "data";

    private int mCanBus;
    private int mId;
    private byte[] mData = new byte[8];

    public CanMessage(Map<String, Object> values) {
        if(!matchesKeys(values)) {
            // TODO raise exceptoin
        }
        init((Integer)values.get(BUS_KEY), (Integer)values.get(ID_KEY),
                (byte[])values.get(CanMessage.DATA_KEY));
    }

    public CanMessage(int canBus, int id, byte[] data) {
        init(canBus, id, data);
    }

    private void init(int canBus, int id, byte[] data)  {
        mCanBus = canBus;
        mId = id;
        System.arraycopy(data, 0, mData, 0, data.length);
    }

    public int getCanBus() {
        return mCanBus;
    }
    public int getId() {
        return mId;
    }

    public byte[] getData() {
        return mData;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        writeMinimalToParcel(out, flags);
        out.writeInt(getCanBus());
        out.writeInt(getId());
        out.writeByteArray(getData());
    }

    public void readFromParcel(Parcel in) {
        readMinimalFromParcel(in);
        mCanBus = in.readInt();
        mId = in.readInt();
        in.readByteArray(mData);
    }

    protected static boolean matchesKeys(Map<String, Object> map) {
        return map.containsKey(BUS_KEY) && map.containsKey(ID_KEY)
                && map.containsKey(DATA_KEY);
    }

    private CanMessage(Parcel in) throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    private CanMessage() { }
}
