package com.openxc.messages;

import java.util.Map;
import java.util.HashMap;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class CommandResponse extends CommandMessage {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String MESSAGE_KEY = "message";
    private String mMessage;

    public CommandResponse(Map<String, Object> values) {
        this(null, values);
    }

    public CommandResponse(String command, Map<String, Object> values) {
        super(command, values);
        initFromValues();
    }

    private void initFromValues() {
        if(contains(COMMAND_RESPONSE_KEY)) {
            init((String) getValuesMap().remove(COMMAND_RESPONSE_KEY));
        }
        mMessage = (String) getValuesMap().remove(MESSAGE_KEY);
    }

    public String getMessage() {
        return mMessage;
    }

    private CommandResponse(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    protected static boolean matchesKeys(Map<String, Object> map) {
        return map.containsKey(CommandResponse.COMMAND_RESPONSE_KEY);
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(CommandMessage.COMMAND_KEY, getCommand());
        return new MessageKey(key);
    }

    protected CommandResponse() { }
}
