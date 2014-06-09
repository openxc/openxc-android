package com.openxc.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Parcel;

public class DiagnosticResponse extends DiagnosticMessage {

    public static final String VALUE_KEY = "value";
    public static final String NEGATIVE_RESPONSE_CODE_KEY = "negative_response_code";
    public static final String SUCCESS_KEY = "success";

    private boolean mSuccess = false;
    private float mValue;
    private NegativeResponseCode mNegativeResponseCode;
    
    public interface Listener {
        public void receive(DiagnosticRequest req, DiagnosticResponse response);
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
            return "0x" + Integer.toHexString(this.code);
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

    public DiagnosticResponse(Map<String, Object> values) {
        super(values);
        if (mSuccess = (boolean) values.get(SUCCESS_KEY)) {
                mNegativeResponseCode = NegativeResponseCode.NONE;
        } else {
            mNegativeResponseCode = (NegativeResponseCode) values
                    .get(NEGATIVE_RESPONSE_CODE_KEY);
        }
        if (values.containsKey(VALUE_KEY)) {
            mValue = (float) values.get(VALUE_KEY);
        }
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
    public boolean equals(Object obj) {
        if (obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticResponse other = (DiagnosticResponse) obj;
        return super.equals(other) && (mSuccess == other.mSuccess)
                && (mValue == other.mValue)
                && (mNegativeResponseCode == other.mNegativeResponseCode);
    }

    //TODO this is a guess, not 100% sure how this parcel stuff fits in
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeByte((byte) (getSuccess() ? 1 : 0));
        out.writeFloat(getValue());
        out.writeInt(getNegativeResponseCode().code());
    }

    //TODO this is a guess, not 100% sure how this parcel stuff fits in
    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mSuccess = in.readByte() != 0;
        mValue = in.readFloat();
        mNegativeResponseCode = NegativeResponseCode.get(in.readInt());
    }

    protected static boolean matchesKeys(Map<String, Object> map) {
        return DiagnosticMessage.matchesKeys(map) &&
                map.containsKey(DiagnosticResponse.SUCCESS_KEY);
    }
}
