package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;

import com.google.common.base.Objects;

public class CommandResponse extends VehicleMessage implements KeyedMessage {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String MESSAGE_KEY = "message";

    private String mCommand;
    // Message is optional
    private String mMessage;

    public CommandResponse(String command, String message) {
        mCommand = command;
        mMessage = message;
    }

    public CommandResponse(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
        if(!containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }
        mCommand = (String) getValuesMap().remove(COMMAND_RESPONSE_KEY);
        setMessage(getValuesMap());
    }

    public CommandResponse(String command, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(values);
        mCommand = command;
        setMessage(values);
    }

    private void setMessage(Map<String, Object> values) {
        mMessage = (String) values.remove(MESSAGE_KEY);
    }

    public String getMessage() {
        return mMessage;
    }

    public String getCommand() {
        return mCommand;
    }

    public static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(COMMAND_RESPONSE_KEY);
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
