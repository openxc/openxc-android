package com.openxc.messages;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * A Command message defined by the OpenXC message format.
 * <p>
 * Commands are keyed on the command name.
 */
public class Command extends KeyedMessage {
    protected static final String COMMAND_KEY = "command";
    protected static final String DIAGNOSTIC_REQUEST_KEY = "request";
    protected static final String ACTION_KEY = "action";
    protected static final String BUS_KEY = "bus";
    protected static final String ENABLED_KEY = "enabled";
    protected static final String BYPASS_KEY = "bypass";
    protected static final String FORMAT_KEY = "format";
    protected static final String UNIX_TIME_KEY = "unix_time";
    public enum CommandType {
        VERSION, DEVICE_ID, DIAGNOSTIC_REQUEST, PLATFORM, PASSTHROUGH, AF_BYPASS, PAYLOAD_FORMAT
        , SD_MOUNT_STATUS, RTC_CONFIGURATION
    }

    private static final String[] sRequiredFieldsValues = new String[]{
            COMMAND_KEY};
    private static final Set<String> sRequiredFields = new HashSet<>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(COMMAND_KEY)
    private CommandType mCommand;

    @SerializedName(ACTION_KEY)
    private String mAction;

    @SerializedName(DIAGNOSTIC_REQUEST_KEY)
    private DiagnosticRequest mDiagnosticRequest;

    @SerializedName(BUS_KEY)
    private int mBus;

    @SerializedName(ENABLED_KEY)
    private boolean mEnabled;

    @SerializedName(BYPASS_KEY)
    private boolean mBypass;

    @SerializedName(FORMAT_KEY)
    private String mFormat;

    @SerializedName(UNIX_TIME_KEY)
    private long mUnixTime;

    public Command(CommandType command, int bus, boolean enabled) {
        mCommand = command;
        mBus = bus;
        mEnabled = enabled;
    }

    public Command(CommandType command, boolean bypass, int bus) {
        mCommand = command;
        mBus = bus;
        mBypass = bypass;
    }

    public Command(String format,CommandType command) {
        mFormat = format;
        mCommand = command;
    }

    public Command(CommandType command, long unixTime) {
        this.mCommand = command;
        this.mUnixTime = unixTime;
    }

    public Command(CommandType command, String action) {
        mCommand = command;
        mAction = action;
    }

    public Command(CommandType command) {
        this(command, null);
    }

    public Command(DiagnosticRequest request, String action) {
        this(CommandType.DIAGNOSTIC_REQUEST, action);
        mDiagnosticRequest = request;
    }

    public CommandType getCommand() {
        return mCommand;
    }

    public boolean hasAction() {
        return mAction != null && !mAction.isEmpty();
    }

    public String getAction() {
        return mAction;
    }

    public DiagnosticRequest getDiagnosticRequest() {
        return mDiagnosticRequest;
    }

    public int getBus() {
        return mBus;
    }

    public void setBus(int mBus) {
        this.mBus = mBus;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public boolean isBypass() {
        return mBypass;
    }

    public void setBypass(boolean bypass) {
        this.mBypass = bypass;
    }

    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String Format) {
        this.mFormat = Format;
    }

    public long getUnixTime() {
        return mUnixTime;
    }

    public void setUnixTime(long unixTime) {
        this.mUnixTime = unixTime;
    }

    @Override
    public MessageKey getKey() {
        if (super.getKey() == null) {
            HashMap<String, Object> key = new HashMap<>();
            key.put(COMMAND_KEY, getCommand());
            setKey(new MessageKey(key));
        }
        return super.getKey();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof Command)) {
            return false;
        }

        final Command other = (Command) obj;
        return Objects.equal(getCommand(), other.getCommand()) &&
                Objects.equal(getDiagnosticRequest(),
                        other.getDiagnosticRequest()) &&
                Objects.equal(getAction(), other.getAction()) &&
                Objects.equal(getBus(), other.getBus()) &&
                Objects.equal(isEnabled(), other.isEnabled()) &&
                Objects.equal(isBypass(), other.isBypass()) &&
                Objects.equal(getFormat(), other.getFormat()) &&
                Objects.equal(getUnixTime(), other.getUnixTime());
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("timestamp", getTimestamp())
                .add("command", getCommand())
                .add("bus", getBus())
                .add("enabled", isEnabled())
                .add("bypass", isBypass())
                .add("format", getFormat())
                .add("unix_time",getUnixTime())
                .add("action", getAction())
                .add("diagnostic_request", getDiagnosticRequest())
                .add("extras", getExtras())
                .toString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(getCommand());
        out.writeString(getAction());
        out.writeParcelable(getDiagnosticRequest(), flags);
        out.writeInt(getBus());
        out.writeByte((byte) (isEnabled() ? 1 : 0));
        out.writeByte((byte) (isBypass() ? 1 : 0));
        out.writeString(getFormat());
        out.writeLong(getUnixTime());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCommand = (CommandType) in.readSerializable();
        mAction = in.readString();
        mDiagnosticRequest = in.readParcelable(DiagnosticRequest.class.getClassLoader());
        mBus = in.readInt();
        mBypass = in.readByte() != 0;
        mEnabled = in.readByte() != 0;
        mFormat = in.readString();
        mUnixTime = in.readLong();
    }

    protected Command(Parcel in) {
        readFromParcel(in);
    }

    protected Command() {
    }
}
