package com.openxc.messages;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class CanMessage extends VehicleMessage {
    public static final String DATA_KEY = "data";
    private int mCanBus;
    private int mId;
    private byte[] mData = new byte[8];

    public CanMessage(int canBus, int id, byte[] data) {
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

    private CanMessage(Parcel in) throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    private CanMessage() { }
}
