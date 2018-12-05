package com.openxc.messages;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;
import com.openxc.messages.ModemCommand.CommandType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ModemCommandResponse extends KeyedMessage {

    public static final String COMMAND_RESPONSE_KEY = "modem_command_response";
    public static final String STATUS_KEY = "status";
    public static final String MESSAGE_KEY = "modem_message";

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

    public ModemCommandResponse(CommandType command, boolean status, String message) {
        mCommand = command;
        mStatus = status;
        mMessage = message;
    }

    public ModemCommandResponse(CommandType command, boolean status) {//TODO: FLEX rename CommandType to ModemCommandType?
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
        if(!super.equals(obj) || !(obj instanceof ModemCommandResponse)) {
            return false;
        }

        final ModemCommandResponse other = (ModemCommandResponse) obj;
        return Objects.equal(mCommand, other.mCommand) &&
                Objects.equal(mMessage, other.mMessage) &&
                Objects.equal(mStatus, other.mStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("modem_command", getCommand())
            .add("status", getStatus())
            .add("modem_message", getMessage())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(ModemCommand.COMMAND_KEY, getCommand());
            setKey(new MessageKey(key));
        }
        return super.getKey();
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

    protected ModemCommandResponse(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected ModemCommandResponse() { }
}
