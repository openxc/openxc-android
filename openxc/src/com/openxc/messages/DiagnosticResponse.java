package com.openxc.messages;

public class DiagnosticResponse extends VehicleMessage {
    private int mCanBus;
    private int mId;
    private int mMode;
    private int mPid;
    private boolean mSuccess;
    private byte[] mPayload;
    private float mValue;
    private NegativeResponseCode mNegativeResponseCode;

    public static enum NegativeResponseCode {
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

        private int value;
        NegativeResponseCode(int value) {
        	this.value = value;        	
        }
        
        public int value() {
        	return this.value;
        }
    };
}
