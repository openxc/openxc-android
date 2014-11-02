package com.openxc.messages;

import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.Parcel;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;

/**
 * A diagnostic response message from the vehicle interface.
 */
public class DiagnosticResponse extends DiagnosticMessage {

    private static final String VALUE_KEY = "value";
    private static final String NEGATIVE_RESPONSE_CODE_KEY = "negative_response_code";
    private static final String SUCCESS_KEY = "success";

    private static final String[] sRequiredFieldsValues = new String[] {
            BUS_KEY, ID_KEY, MODE_KEY, SUCCESS_KEY };
    private static final Set<String> sRequiredFields = new HashSet<String>(
            Arrays.asList(sRequiredFieldsValues));

    @SerializedName(SUCCESS_KEY)
    private boolean mSuccess = false;

    @SerializedName(VALUE_KEY)
    private Double mValue;

    @SerializedName(NEGATIVE_RESPONSE_CODE_KEY)
    private NegativeResponseCode mNegativeResponseCode = NegativeResponseCode.NONE;

    public DiagnosticResponse(int busId, int id, int mode) {
        super(busId, id, mode);
    }

    public DiagnosticResponse(int busId, int id, int mode, int pid,
            byte[] payload) {
        super(busId, id, mode, pid);
        setPayload(payload);
    }

    public DiagnosticResponse(int busId, int id, int mode, int pid,
            byte[] payload,
            NegativeResponseCode negativeResponseCode,
            double value) {
        this(busId, id, mode, pid, payload);
        mNegativeResponseCode = negativeResponseCode == null ?
                NegativeResponseCode.NONE : negativeResponseCode;
        mValue = value;
    }

    public boolean isSuccessful() {
        return mNegativeResponseCode == NegativeResponseCode.NONE;
    }

    public boolean hasValue() {
        return mValue != null;
    }

    public Double getValue() {
        return mValue;
    }

    public void setValue(Double value) {
        mValue = value;
    }

    public NegativeResponseCode getNegativeResponseCode() {
        return mNegativeResponseCode;
    }

    public void setNegativeResponseCode(NegativeResponseCode code) {
        mNegativeResponseCode = code;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("timestamp", getTimestamp())
            .add("id", getId())
            .add("mode", getMode())
            .add("pid", getPid())
            .add("payload", Arrays.toString(getPayload()))
            .add("value", getValue())
            .add("negative_response_code", getNegativeResponseCode())
            .add("extras", getExtras())
            .toString();
    }

    public static boolean containsRequiredFields(Set<String> fields) {
        return fields.containsAll(sRequiredFields);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof DiagnosticResponse)) {
            return false;
        }

        final DiagnosticResponse other = (DiagnosticResponse) obj;
        return Objects.equal(mValue, other.mValue)
                && Objects.equal(mNegativeResponseCode, other.mNegativeResponseCode);
    }

    public static enum NegativeResponseCode {
        NONE(-1),
        GENERAL_REJECT(0x10),
        SERVICE_NOT_SUPPORTED(0x11),
        SUB_FUNCTION_NOT_SUPPORTED(0x12),
        INCORRECT_MESSAGE_LENGTH_OR_INVALID_FORMAT(0x13),
        RESPONSE_TOO_LONG(0x14),
        BUSY_REPEAT_REQUEST(0x21),
        CONDITIONS_NOT_CORRECT(0x22),
        REQUEST_SEQUENCE_ERROR(0x24),
        NO_RESPONSE_FROM_SUBNET_COMPONENT(0x25),
        FAILURE_PREVENTS_EXECUTION_OF_REQUESTED_ACTION(0x26),
        REQUEST_OUT_OF_RANGE(0x31),
        SECURITY_ACCESS_DENIED(0x33),
        INVALID_KEY(0x35),
        EXCEED_NUMBER_OF_ATTEMPTS(0x36),
        REQUIRED_TIME_DELAY_NOT_EXPIRED(0x37),
        UPLOAD_DOWNLOAD_NOT_ACCEPTED(0x70),
        TRANSFER_DATA_SUSPENDED(0x71),
        GENERAL_PROGRAMMING_FAILURE(0x72),
        WRONG_BLOCK_SEQUENCE_COUNTER(0x73),
        REQUEST_CORRECTLY_RECEIVED_RESPONSE_PENDING(0X78),
        SUB_FUNCTION_NOT_SUPPORTED_IN_ACTIVE_SESSION(0x7E),
        SERVICE_NOT_SUPPORTED_IN_ACTIVE_SESSION(0x7F),
        RPM_TOO_HIGH(0x81),
        RPM_TOO_LOW(0x82),
        ENGINE_IS_RUNNING(0x83),
        ENGINE_IS_NOT_RUNNING(0x84),
        ENGINE_RUN_TIME_TOO_LOW(0x85),
        TEMPERATURE_TOO_HIGH(0x86),
        TEMPERATURE_TOO_LOW(0x87),
        VEHICLE_SPEED_TOO_HIGH(0x88),
        VEHICLE_SPEED_TOO_LOW(0x89),
        THROTTLE_PEDAL_TOO_HIGH(0x8A),
        THROTTLE_PEDAL_TOO_LOW(0x8B),
        TRANSMISSION_RANGE_NOT_IN_NEUTRAL(0x8C),
        TRANSMISSION_RANGE_NOT_IN_GEAR(0x8D),
        BRAKE_SWITCH_ES_NOT_CLOSED(0x8F),
        SHIFTER_LEVER_NOT_IN_PARK(0x90),
        TORQUE_CONVERTER_CLUTCH_LOCKED(0x91),
        VOLTAGE_TOO_HIGH(0x92),
        VOLTAGE_TOO_LOW(0x93);

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
        out.writeValue(getValue());
        out.writeSerializable(getNegativeResponseCode());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mValue = (Double) in.readValue(Double.class.getClassLoader());
        mNegativeResponseCode = (NegativeResponseCode) in.readSerializable();
    }

    protected DiagnosticResponse(Parcel in)
            throws UnrecognizedMessageTypeException {
        readFromParcel(in);
    }

    protected DiagnosticResponse() { }
}
