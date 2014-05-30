package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class CommandMessage extends NamedVehicleMessage {

    public static final String COMMAND_KEY = "command";
	
    public CommandMessage(String command, Map<String, Object> values) {
        super(command, values);
    }

    public String getCommandName() {
        return getName();
    }

    private CommandMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    protected CommandMessage() { }
}
