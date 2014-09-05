package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import com.openxc.messages.Command.CommandType;

public class CommandResponse extends KeyedMessage {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String STATUS_KEY = "status";
    public static final String MESSAGE_KEY = "message";

    public static final String[] sRequiredFieldsValues = new String[] {
            COMMAND_RESPONSE_KEY, STATUS_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_RESPONSE_KEY)
    private CommandType mCommand;

    @SerializedName(STATUS_KEY)
    private boolean mStatus;

    // Message is optional
    @SerializedName(MESSAGE_KEY)
    private String mMessage;

    public CommandResponse(CommandType command, boolean status, String message) {
        mCommand = command;
        mStatus = status;
        mMessage = message;
    }

    public CommandResponse(CommandType command, boolean status) {
        this(command, status, null);
    }

    public boolean hasMessage() {
        return mMessage != null;
    }

    public String getMessage() {
        return mMessage;
    }

    public CommandType getCommand() {
        return mCommand;
    }

    public boolean getStatus() {
        return mStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || !(obj instanceof CommandResponse)) {
            return false;
        }

        final CommandResponse other = (CommandResponse) obj;
        return Objects.equal(mCommand, other.mCommand) &&
                Objects.equal(mMessage, other.mMessage) &&
                Objects.equal(mStatus, other.mStatus);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("command", getCommand())
            .add("status", getStatus())
            .add("message", getMessage())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(Command.COMMAND_KEY, getCommand());
        return new MessageKey(key);
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(getCommand());
        out.writeInt(getStatus() ? 1 : 0);
        out.writeString(getMessage());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCommand = (CommandType) in.readSerializable();
        mStatus = in.readInt() == 1;
        mMessage = in.readString();
    }

    protected CommandResponse(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected CommandResponse() { }
}
