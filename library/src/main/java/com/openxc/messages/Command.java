package com.openxc.messages;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * A Command message defined by the OpenXC message format.
 *
 * Commands are keyed on the command name.
 */
public class Command extends KeyedMessage {
    protected static final String COMMAND_KEY = "command";
    protected static final String DIAGNOSTIC_REQUEST_KEY = "request";
    protected static final String ACTION_KEY = "action";
    protected static final String DATA_KEY = "data";
    protected static final String NUMBER_KEY = "number";
    protected static final String SIZE_KEY ="size";

    public enum CommandType {
        VERSION, DEVICE_ID, DIAGNOSTIC_REQUEST, PLATFORM, UPDATE_REQUEST
    }

    private static final String[] sRequiredFieldsValues = new String[] {
            COMMAND_KEY, DATA_KEY, NUMBER_KEY, SIZE_KEY }; //should DATA_KEY be a required key?
    private static final Set<String> sRequiredFields = new HashSet<>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_KEY)
    private CommandType mCommand;

    @SerializedName(ACTION_KEY)
    private String mAction;

    @SerializedName(DIAGNOSTIC_REQUEST_KEY)
    private DiagnosticRequest mDiagnosticRequest;

    @SerializedName(DATA_KEY)
    private byte[] mData;

    @SerializedName(NUMBER_KEY)
    private int mNum;

    @SerializedName(SIZE_KEY)
    private double mSize;

    public Command(CommandType command, String action, byte[] data, int num, double size) {
        mCommand = command;
        //action could be for diagnostic or for update (Start, Stop, File)
        mAction = action;
        //data is for the transfer of file
        if (data == null) {
            mData = null;
        } else {
            mData = new byte[128];
            setPayload(data);
        }
        //num is to serialize the data chunks
        mNum = num;
        //size is to send the size of the file
        mSize = size;
    }

    public Command(CommandType command) {
        this(command, null, null, 0, 0);
    }

    public Command(CommandType command, String action) {
        this(command, action, null, 0, 0);
    }

    public Command(DiagnosticRequest request, String action) {
        this(CommandType.DIAGNOSTIC_REQUEST, action, null, 0, 0);
        mDiagnosticRequest = request;
    }

    public Command(CommandType command, String action, byte[] data, int num) {
        this(command, action, data, num, 0);
    }

    public Command(CommandType command, String action, double size) {
        this(command, action, null, 0, size);
    }

    private void setPayload(byte[] data) {
        if(data != null) {
            System.arraycopy(data, 0, mData, 0, data.length);
        }
    }

    public CommandType getCommand() {
        return mCommand;
    }

    public boolean hasAction() {
        return mAction != null && !mAction.isEmpty();
    }

    public String getAction() {
        return mAction;
    }

    public boolean hasData() {
        return mData != null;
    }

    public byte[] getData() {
        return mData;
    }

    public int getCount() {
        return mNum;
    }

    public double getSize() {
        return mSize;
    }

    public DiagnosticRequest getDiagnosticRequest() {
        return mDiagnosticRequest;
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(COMMAND_KEY, getCommand());
            setKey(new MessageKey(key));
        }
        return super.getKey();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || !(obj instanceof Command)) {
            return false;
        }

        final Command other = (Command) obj;
        return Objects.equal(getCommand(), other.getCommand()) &&
                Objects.equal(getDiagnosticRequest(),
                        other.getDiagnosticRequest()) &&
                Objects.equal(getAction(), other.getAction());
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("command", getCommand())
            .add("action", getAction())
            .add("diagnostic_request", getDiagnosticRequest())
            .add("data", getData())
            .add("count", getCount())
            .add("size", getSize())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(getCommand());
        out.writeString(getAction());
        out.writeByteArray(getData());
        out.writeInt(getCount());
        out.writeDouble(getSize());
        out.writeParcelable(getDiagnosticRequest(), flags);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCommand = (CommandType) in.readSerializable();
        mAction = in.readString();
        //mData = in.readByteArray();
        mNum = in.readInt();
        mSize = in.readDouble();
        mDiagnosticRequest = in.readParcelable(DiagnosticRequest.class.getClassLoader());
    }

    protected Command(Parcel in) {
        readFromParcel(in);
    }

    protected Command() { }
}
