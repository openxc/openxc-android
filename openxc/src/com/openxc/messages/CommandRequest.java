package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.google.common.base.Objects;

public class CommandRequest extends Command  {

    public static final String COMMAND_KEY = "command";
    
    public CommandRequest(String command) {
        super(command);
    }

    public CommandRequest(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
    }

    public CommandRequest(String command, Map<String, Object> values)
            throws InvalidMessageFieldsException {
        super(command, values);
    }
    
    @Override
    public String getCommandKey() {
        return COMMAND_KEY;
    }   

    public static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(COMMAND_KEY);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final CommandRequest other = (CommandRequest) obj;
        return getCommand().equals(other.getCommand());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("command", getCommand())
            .add("values", getValuesMap())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getCommand());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        setCommand(in.readString());
    }

    protected CommandRequest(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected CommandRequest() { }
    
}
