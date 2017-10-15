package com.openxc.messages;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * A Custom Command message based on the OpenXC message format.
 * Commands are keyed on the command name.
 */
public class CustomCommand extends KeyedMessage {
    protected static final String COMMAND_KEY = "command";

    private HashMap<String, String> commands = new HashMap<>();

    public CustomCommand(HashMap commands) {
        this.commands = commands;
    }

    public HashMap<String, String> getCommands() {
        return commands;
    }

    @Override
    public MessageKey getKey() {
        if (super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(COMMAND_KEY, commands.get(COMMAND_KEY));
            setKey(new MessageKey(key));
        }
        return super.getKey();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof CustomCommand)) {
            return false;
        }

        final CustomCommand other = (CustomCommand) obj;
        return Objects.equal(getCommands(), other.getCommands());
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper finalString = toStringHelper(COMMAND_KEY);
        finalString.add("timestamp", getTimestamp());
        for (Map.Entry<String, String> entry : commands.entrySet()) {
            finalString.add(entry.getKey(), entry.getValue());
        }

        return finalString.toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        final int N = commands.size();
        out.writeInt(N);
        if (N > 0) {
            for (Map.Entry<String, String> entry : commands.entrySet()) {
                out.writeString(entry.getKey());
                String dat = entry.getValue();
                out.writeString(dat);
            }
        }
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        final int N = in.readInt();
        for (int i = 0; i < N; i++) {
            String key = in.readString();
            String value = in.readString();
            commands.put(key, value);
        }
    }

    protected CustomCommand(Parcel in) {
        readFromParcel(in);
    }

    protected CustomCommand() {
    }
}
