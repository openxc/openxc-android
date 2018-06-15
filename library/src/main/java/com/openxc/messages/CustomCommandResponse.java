package com.openxc.messages;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A response to a Custom Command from the vehicle interface.
 *
 * Custom Command responses have the same key as the original custom command request.
 *
 * Only difference between custom command response and command response is type of command.
 */
public class CustomCommandResponse extends KeyedMessage {

    private static final String COMMAND_RESPONSE_KEY = "command_response";
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";

    private static final String[] sRequiredFieldsValues = new String[] {
            COMMAND_RESPONSE_KEY, STATUS_KEY };
    private static final Set<String> sRequiredFields = new HashSet<>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_RESPONSE_KEY)
    private String command;

    @SerializedName(STATUS_KEY)
    private boolean status;

    // Message is optional
    @SerializedName(MESSAGE_KEY)
    private String message;

    public String getMessage() {
        return message;
    }

    public String getCommand() {
        return command;
    }
    public void setCommand(String mCommand) {
        this.command = mCommand;
    }
    public boolean getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj) || !(obj instanceof CustomCommandResponse)) {
            return false;
        }

        final CustomCommandResponse other = (CustomCommandResponse) obj;
        return Objects.equal(command, other.command) &&
                Objects.equal(message, other.message) &&
                Objects.equal(status, other.status);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", getTimestamp())
                .add("command", getCommand())
                .add("status", getStatus())
                .add("message", getMessage())
                .add("extras", getExtras())
                .toString();
    }

    @Override
    public MessageKey getKey() {
        if(super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(Command.COMMAND_KEY, getCommand());
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
        out.writeString(getCommand());
        out.writeInt(getStatus() ? 1 : 0);
        out.writeString(getMessage());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        command = in.readString();
        status = in.readInt() == 1;
        message = in.readString();
    }

    protected CustomCommandResponse(Parcel in) {
        readFromParcel(in);
    }

    protected CustomCommandResponse() { }
}
