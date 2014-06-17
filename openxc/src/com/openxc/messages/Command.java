package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.google.common.base.Objects;

public class Command extends VehicleMessage implements KeyedMessage {
    public static final String COMMAND_KEY = "command";

    @SerializedName(COMMAND_KEY)
    private String mCommand;

    public Command(String command) {
        mCommand = command;
    }

    public Command(Map<String, Object> values) throws InvalidMessageFieldsException {
        super(values);
        if(!containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }
        mCommand = (String) getValuesMap().remove(COMMAND_KEY);
    }

    public Command(String command, Map<String, Object> values) {
        super(values);
        mCommand = command;
    }

    public String getCommand() {
        return mCommand;
    }

    public static boolean containsAllRequiredFields(Map<String, Object> map) {
        return map.containsKey(COMMAND_KEY);
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(COMMAND_KEY, getCommand());
        return new MessageKey(key);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final Command other = (Command) obj;
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
        mCommand = in.readString();
    }

    protected Command(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected Command() { }

}
