package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

/**
 * A VehicleMessage that is a low-level CAN message.
 *
 * A CAN message is keyed on the bus and message ID.
 */
public class CanMessage extends KeyedMessage {
    protected static final String ID_KEY = "id";
    protected static final String BUS_KEY = "bus";
    protected static final String DATA_KEY = "data";

    @SerializedName(BUS_KEY)
    private int mBusId;

    @SerializedName(ID_KEY)
    private int mId;

    @SerializedName(DATA_KEY)
    private byte[] mData = new byte[8];

    private static final String[] sRequiredFieldsValues = new String[] {
            BUS_KEY, ID_KEY, DATA_KEY };
    private static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    public CanMessage(int canBus, int id, byte[] data) {
        mBusId = canBus;
        mId = id;
        setPayload(data);
    }

    public int getBusId() {
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

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(BUS_KEY, getBusId());
            key.put(ID_KEY, getId());
            setKey(new MessageKey(key));
        }
        return super.getKey();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    /**
     * Sort by bus, then by message ID.
     */
    @Override
    public int compareTo(VehicleMessage other) {
        CanMessage otherMessage = (CanMessage) other;
        if(getBusId() < otherMessage.getBusId()) {
            return -1;
        } else if(getBusId() > otherMessage.getBusId()) {
            return 1;
        } else {
            if(getId() < otherMessage.getId()) {
                return -1;
            } else if(getId() > otherMessage.getId()) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || !(obj instanceof CanMessage)) {
            return false;
        }

        final CanMessage other = (CanMessage) obj;
        return mId == other.mId && mBusId == other.mBusId
                && Arrays.equals(mData, other.mData);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("bus", getBusId())
            .add("id", getId())
            .add("data", Arrays.toString(getData()))
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(getBusId());
        out.writeInt(getId());
        out.writeByteArray(getData());
    }

    @Override
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
