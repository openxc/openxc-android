package com.openxc.messages;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

public class DiagnosticResponse extends DiagnosticMessage {

    public static final String VALUE_KEY = "value";
    public static final String NEGATIVE_RESPONSE_CODE_KEY = "negative_response_code";
    public static final String SUCCESS_KEY = "success";

    public static final String[] sRequiredFieldsValues = new String[] {
            BUS_KEY, ID_KEY, MODE_KEY, SUCCESS_KEY };
    public static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(SUCCESS_KEY)
    private boolean mSuccess = false;

    // TODO need a way to say 'no value' so you know to look at payload
    @SerializedName(VALUE_KEY)
    private float mValue;

    @SerializedName(NEGATIVE_RESPONSE_CODE_KEY)
    private NegativeResponseCode mNegativeResponseCode = NegativeResponseCode.NONE;

    public DiagnosticResponse(int busId, int id, int mode, int pid,
            byte[] payload,
            // TODO rather than construct with success, construct with negative
            // response code only if it failed. otherwise we assume it was
            // successful.
            boolean success) {
        super(busId, id, mode, pid);
        setPayload(payload);
        mSuccess = success;
    }

    public DiagnosticResponse(int busId, int id, int mode, int pid,
            byte[] payload,
            boolean success,
            NegativeResponseCode negativeResponseCode,
            float value) {
        this(busId, id, mode, pid, payload, success);
        mNegativeResponseCode = negativeResponseCode;
        mValue = value;
    }

    public boolean getSuccess() {
        return mSuccess;
    }

    public float getValue() {
        return mValue;
    }

    public NegativeResponseCode getNegativeResponseCode() {
        return mNegativeResponseCode;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("id", getId())
            .add("mode", getMode())
            .add("pid", getPid())
            .add("payload", Arrays.toString(getPayload()))
            .add("value", getValue())
            .add("negative_response_code", getNegativeResponseCode())
            .add("success", getSuccess())
            .add("extras", getExtras())
            .toString();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticResponse other = (DiagnosticResponse) obj;
        return mSuccess == other.mSuccess
                && mValue == other.mValue
                && mNegativeResponseCode == other.mNegativeResponseCode;
    }

    public interface Listener {
        public void receive(DiagnosticRequest request, DiagnosticResponse response);
    }

    public static enum NegativeResponseCode {
        NONE(-1),
        SUB_FUNCTION_NOT_SUPPORTED(0x12),
        INCORRECT_MESSAGE_LENGTH_OR_INVALID_FORMAT(0x13),
        BUSY_REPEAT_REQUEST(0x21),
        CONDITIONS_NOT_CORRECT(0x22),
        REQUEST_SEQUENCE_ERROR(0x24),
        REQUEST_OUT_OF_RANGE(0x31),
        SECURITY_ACCESS_DENIED(0x33),
        INVALID_KEY(0x35),
        EXCEED_NUMBER_OF_ATTEMPTS(0x36),
        REQUIRED_TIME_DELAY_NOT_EXPIRED(0x37),
        UPLOAD_DOWNLOAD_NOT_ACCEPTED(0x70),
        WRONG_BLOCK_SEQUENCE_COUNTER(0x73),
        SUB_FUNCTION_NOT_SUPPORTED_IN_ACTIVE_SESSION(0x7E),
        SERVICE_NOT_SUPPORTED_IN_ACTIVE_SESSION(0x7F);

        private int code;
        private static final Map<Integer, NegativeResponseCode> lookup = new HashMap<>();

        static {
            for (NegativeResponseCode c : NegativeResponseCode.values()) {
                lookup.put(c.code(), c);
            }
        }

        private NegativeResponseCode(int value) {
            this.code = value;
        }

        public int code() {
            return this.code;
        }

        public static NegativeResponseCode get(int value) {
            return lookup.get(value);
        }

        public String hexCodeString() {
            return "0x" + Integer.toHexString(this.code).toUpperCase(Locale.US);
        }

        public String toDocumentationString() {
            String result = this.toString().toLowerCase(Locale.US);
            String und = "_";
            while (result.contains(und)) {
                int underscoreIndex = result.indexOf(und);
                result = result.substring(0, underscoreIndex) +
                        String.valueOf(result.charAt(underscoreIndex + 1)).toUpperCase(Locale.US)
                        + result.substring(underscoreIndex + 2);
            }
            return result;
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (getSuccess() ? 1 : 0));
        out.writeFloat(getValue());
        out.writeSerializable(getNegativeResponseCode());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mSuccess = in.readByte() != 0;
        mValue = in.readFloat();
        mNegativeResponseCode = (NegativeResponseCode) in.readSerializable();
    }

    protected DiagnosticResponse(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected DiagnosticResponse() { }
}
