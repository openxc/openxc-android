package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class CommandResponse extends VehicleMessage implements KeyedMessage {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String MESSAGE_KEY = "message";

    public static final String[] sRequiredFieldsValues = new String[] {
            COMMAND_RESPONSE_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_RESPONSE_KEY)
    private String mCommand;

    // Message is optional
    @SerializedName(MESSAGE_KEY)
    private String mMessage;

    public CommandResponse(String command, String message) {
        mCommand = command;
        mMessage = message;
    }

    public CommandResponse(String command, String message,
            Map<String, Object> extraValues)
            throws InvalidMessageFieldsException {
        super(extraValues);
        mCommand = command;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getCommand() {
        return mCommand;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final CommandResponse other = (CommandResponse) obj;
        return (mMessage == null && other.mMessage == null) ||
                mMessage.equals(other.mMessage);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("command", getCommand())
            .add("message", getMessage())
            .add("values", getValuesMap())
            .toString();
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(COMMAND_RESPONSE_KEY, getCommand());
        return new MessageKey(key);
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getMessage());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mMessage = in.readString();
    }

    protected CommandResponse(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected CommandResponse() { }
}
