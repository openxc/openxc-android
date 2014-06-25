package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class Command extends VehicleMessage implements KeyedMessage {
    public static final String COMMAND_KEY = "command";

    public static final String[] sRequiredFieldsValues = new String[] {
            COMMAND_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_KEY)
    private String mCommand;

    public Command(String command, Map<String, Object> extras) {
        super(extras);
        mCommand = command;
    }

    public Command(String command) {
        this(command, null);
        mCommand = command;
    }

    public String getCommand() {
        return mCommand;
    }

    public MessageKey getKey() {
        HashMap<String, Object> key = new HashMap<>();
        key.put(COMMAND_KEY, getCommand());
        return new MessageKey(key);
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
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
            .add("extras", getExtras())
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
