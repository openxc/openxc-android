package com.openxc.messages;

import java.util.Map;
import java.util.HashMap;

import android.os.Parcel;

import com.google.common.base.Objects;

public class CommandResponse extends CommandMessage {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String MESSAGE_KEY = "message";
    private String mMessage;

    public CommandResponse(Map<String, Object> values) throws MismatchedMessageKeysException {
        if(!containsSameKeySet(values)) {
            throw new MismatchedMessageKeysException(
                    "Missing keys for CommandResponse construction in values = " +
                    values.toString());
        }
        setCommand((String) values.remove(COMMAND_RESPONSE_KEY));
        setMessage(values);
    }

    public CommandResponse(String command, String message) {
        super(command);
        mMessage = message;
    }

    public CommandResponse(String command, Map<String, Object> values) {
        super(command, values);
        setMessage(values);
    }

    protected void setMessage(Map<String, Object> values) {
        mMessage = (String) values.remove(MESSAGE_KEY);
    }

    public String getMessage() {
        return mMessage;
    }

    protected static boolean containsSameKeySet(Map<String, Object> map) {
        return map.containsKey(CommandResponse.COMMAND_RESPONSE_KEY);
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

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(CommandMessage.COMMAND_KEY, getCommand());
        return new MessageKey(key);
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
