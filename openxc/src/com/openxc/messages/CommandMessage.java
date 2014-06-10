package com.openxc.messages;

import java.util.Map;
import java.util.HashMap;

import android.os.Parcel;

import com.google.common.base.Objects;

public class CommandMessage extends VehicleMessage implements KeyedMessage {
    public static final String COMMAND_KEY = "command";

    private String mCommand;

    public CommandMessage(String command) {
        mCommand = command;
    }

    public CommandMessage(Map<String, Object> values) {
        super(values);
        if(!matchesKeys(values)) {
            // TODO raise exception
        }
        setCommand(getValuesMap());
    }

    public CommandMessage(String command, Map<String, Object> values) {
        super(values);
    }

    protected void setCommand(String command) {
        mCommand = command;
    }

    protected void setCommand(Map<String, Object> values) {
        setCommand((String) values.remove(COMMAND_KEY));
    }

    public String getCommand() {
        return mCommand;
    }

    protected static boolean matchesKeys(Map<String, Object> map) {
        return map.containsKey(COMMAND_KEY);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final CommandMessage other = (CommandMessage) obj;
        return mCommand.equals(other.mCommand);
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(COMMAND_KEY, getCommand());
        return new MessageKey(key);
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
        mCommand = in.readString();
    }

    protected CommandMessage(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected CommandMessage() { }
}
