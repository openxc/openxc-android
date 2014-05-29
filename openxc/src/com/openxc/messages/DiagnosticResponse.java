package com.openxc.messages;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;

public class DiagnosticResponse extends DiagnosticMessage {

	public static final String VALUE_KEY = "value";
	public static final String NEGATIVE_RESPONSE_CODE_KEY = "negative_response_code";
	public static final String SUCCESS_KEY = "success";

	private boolean mSuccess = false;
	private float mValue;
	private NegativeResponseCode mNegativeResponseCode;

	public static enum NegativeResponseCode {
		none(-1),
		subFunctionNotSupported(0x12),
		incorrectMessageLengthOrInvalidFormat(0x13),
		busyRepeatRequest(0x21),
		conditionsNotCorrect(0x22),
		requestSequenceError(0x24),
		requestOutOfRange(0x31),
		securityAccessDenied(0x33),
		invalidKey(0x35),
		exceedNumberOfAttempts(0x36),
		requiredTimeDelayNotExpired(0x37),
		uploadDownloadNotAccepted(0x70),
		wrongBlockSequenceCounter(0x73),
		subFunctionNotSupportedInActiveSession(0x7E),
		serviceNotSupportedInActiveSession(0x7F);

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
			return "0x" + Integer.toHexString(code);
		}
	};

	public DiagnosticResponse(Map<String, Object> values) {
		super(values);
		if (values != null) {
			if (values.containsKey(SUCCESS_KEY)) {
				if (mSuccess = (boolean) values.get(SUCCESS_KEY)) {
					mNegativeResponseCode = NegativeResponseCode.none;
				} else {
					mNegativeResponseCode = (NegativeResponseCode) values
							.get(NEGATIVE_RESPONSE_CODE_KEY);
				}
			}
			if (values.containsKey(VALUE_KEY)) {
				mValue = (float) values.get(VALUE_KEY);
			}
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

}
