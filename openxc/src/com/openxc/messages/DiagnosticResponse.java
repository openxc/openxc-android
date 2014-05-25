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

    public enum NegativeResponseCode {
        // TODO
    };
}
