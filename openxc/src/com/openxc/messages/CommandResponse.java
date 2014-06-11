package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.google.common.base.Objects;

public class CommandResponse extends Command {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String MESSAGE_KEY = "message";
    private String mMessage;

    public CommandResponse(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
        if(!containsRequiredPrimeFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }
        setMessage(values);
    }

    public CommandResponse(String command, String message) {
        super(command);
        setMessage(message);
    }

    public CommandResponse(String command, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(command, values);
        if(!containsRequiredPrimeFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }
        setMessage(values);
    }
    
    private void setMessage(String message) {
        mMessage = message;
    }

    private void setMessage(Map<String, Object> values) {
        setMessage((String) values.remove(MESSAGE_KEY));
    }

    public String getMessage() {
        return mMessage;
    }
    
    @Override
    public String getCommandKey() {
        return COMMAND_RESPONSE_KEY;
    }
    
    public static boolean containsRequiredPrimeFields(Map<String, Object> map) {
        return map.containsKey(MESSAGE_KEY);
    }
    
    public static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(COMMAND_RESPONSE_KEY) 
                && containsRequiredPrimeFields(map);
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
