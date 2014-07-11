package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;

import static com.google.common.base.Objects.toStringHelper;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class Command extends KeyedMessage {
    public static final String COMMAND_KEY = "command";
    // TODO this name should be made more specific
    public static final String DIAGNOSTIC_REQUEST_KEY = "request";

    public enum CommandType {
        VERSION, DEVICE_ID, DIAGNOSTIC_REQUEST
    };

    public static final String[] sRequiredFieldsValues = new String[] {
            COMMAND_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_KEY)
    private CommandType mCommand;

    @SerializedName(DIAGNOSTIC_REQUEST_KEY)
    private DiagnosticRequest mDiagnosticRequest;

    public Command(CommandType command) {
        mCommand = command;
    }

    // TODO this seems really odd, that we're combined these two things. it made
    // more sense when I implemented it in Python. I can't remember...why we are
    // wrapping the DiagRequest in a command? I think it was to make parsing
    // easier on the other end, so you didn't have to try and infer what the
    // incoming datatype was. should we require all input data to be wrapped in
    // a command? that would make more sense but would increase a litle overhead
    // for JSON writes. Let's get this working for now and then change the
    // message format so that we don't use implicit types in JSON anymore - if
    // you need maximum performance, use the binary encoding.
    public Command(DiagnosticRequest request) {
        mCommand = CommandType.DIAGNOSTIC_REQUEST;
        mDiagnosticRequest = request;
    }

    public CommandType getCommand() {
        return mCommand;
    }

    public DiagnosticRequest getDiagnosticRequest() {
        return mDiagnosticRequest;
    }

    @Override
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
        return Objects.equal(getCommand(), other.getCommand()) &&
                Objects.equal(getDiagnosticRequest(),
                        other.getDiagnosticRequest());
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("command", getCommand())
            .add("diagnostic_request", getDiagnosticRequest())
            .add("extras", getExtras())
            .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(getCommand());
        out.writeParcelable(getDiagnosticRequest(), flags);
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCommand = (CommandType) in.readSerializable();
        mDiagnosticRequest = (DiagnosticRequest) in.readParcelable(
                DiagnosticRequest.class.getClassLoader());
    }

    protected Command(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected Command() { }

}
