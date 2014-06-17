package com.openxc.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Parcel;

import com.google.common.primitives.Bytes;
import com.google.gson.annotations.SerializedName;
import com.google.common.base.Objects;

public class DiagnosticResponse extends VehicleMessage implements KeyedMessage {

    public static final String PAYLOAD_KEY = DiagnosticRequest.PAYLOAD_KEY;
    public static final String VALUE_KEY = "value";
    public static final String NEGATIVE_RESPONSE_CODE_KEY = "negative_response_code";
    public static final String SUCCESS_KEY = "success";

    private DiagnosticRequest mRequest;

    @SerializedName(SUCCESS_KEY)
    private boolean mSuccess = false;

    // TODO need a way to say 'no value' so you know to look at payload
    @SerializedName(VALUE_KEY)
    private float mValue;

    @SerializedName(PAYLOAD_KEY)
    private byte[] mPayload;

    @SerializedName(NEGATIVE_RESPONSE_CODE_KEY)
    private NegativeResponseCode mNegativeResponseCode = NegativeResponseCode.NONE;

    public DiagnosticResponse(DiagnosticRequest request, byte[] payload,
            boolean success) {
        mRequest = request;
        setPayload(payload);
        mSuccess = success;
    }

    public DiagnosticResponse(Map<String, Object> values)
            throws InvalidMessageFieldsException {
        if(!DiagnosticRequest.containsAllRequiredFields(values)
                && containsAllRequiredFields(values)) {
            throw new InvalidMessageFieldsException(
                    "Missing keys for construction in values = " +
                    values.toString());
        }

        mRequest = new DiagnosticRequest(values);
        // Kind of weird, but we have to wait for the DiagnosticReuqest to pull
        // out its values from the map before we store a copy, otherwise we'll
        // get a bunch of duplicates. This design smells a bit.
        setValues(mRequest.getValuesMap());
        mSuccess = (boolean) getValuesMap().remove(SUCCESS_KEY);
        if(!mSuccess) {
            mNegativeResponseCode = (NegativeResponseCode)
                getValuesMap().remove(NEGATIVE_RESPONSE_CODE_KEY);
        }

        if(values.containsKey(VALUE_KEY)) {
            mValue = (float) getValuesMap().remove(VALUE_KEY);
        }

        if(contains(DiagnosticRequest.PAYLOAD_KEY)) {
            // Same weird workaround as in CanMessage and DiagnosticRequest
            Object payload = getValuesMap().remove(PAYLOAD_KEY);
            if(payload instanceof ArrayList) {
                setPayload(Bytes.toArray(((ArrayList<Byte>) payload)));
            } else{
                setPayload((byte[]) payload);
            }
        }
    }

    public boolean getSuccess() {
        return mSuccess;
    }

    public float getValue() {
        return mValue;
    }

    public boolean hasPid() {
        return mRequest.hasPid();
    }

    public int getBusId() {
        return mRequest.getBusId();
    }

    public int getId() {
        return mRequest.getId();
    }

    public int getMode() {
        return mRequest.getMode();
    }

    public Integer getPid() {
        return mRequest.getPid();
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public NegativeResponseCode getNegativeResponseCode() {
        return mNegativeResponseCode;
    }

    void setPayload(byte[] payload) {
        if(payload != null) {
            mPayload = new byte[payload.length];
            System.arraycopy(payload, 0, mPayload, 0,
                    Math.min(payload.length, mPayload.length));
        }
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
            .toString();
    }

    public MessageKey getKey() {
        return mRequest.getKey();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !super.equals(obj)) {
            return false;
        }

        // TODO need common fields with DiagnosticRequest
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

    protected static boolean containsAllRequiredFields(Map<String, Object> map) {
        return DiagnosticRequest.containsAllRequiredFields(map) &&
                map.containsKey(DiagnosticResponse.SUCCESS_KEY);
    }

    protected DiagnosticResponse(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected DiagnosticResponse() { }
}
