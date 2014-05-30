package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class CommandResponse extends CommandMessage {

    public static final String COMMAND_RESPONSE_KEY = "command_response";
    public static final String MESSAGE_KEY = "message";
    private String mMessage;
    
    public CommandResponse(String name, Map<String, Object> values) {
        super(name, values);
        if (values.containsKey(MESSAGE_KEY)) {
        	mMessage = (String)values.get(MESSAGE_KEY);
        }
    }
    
    public String getMessage() {
    	return mMessage;
    }
    
    public String getCommandResponse() {
    	return getName();
    }

    private CommandResponse(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    protected CommandResponse() { }
}
