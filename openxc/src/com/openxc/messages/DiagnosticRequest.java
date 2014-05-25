package com.openxc.messages;

public class DiagnosticRequest extends VehicleMessage {
    private int mCanBus;
    private int mId;
    private int mMode;
    private int mPid;
    private byte[] mPayload;
    private boolean mMultipleResponses;
    private float mFactor;
    private float mOffset;
    private float mFrequency;
    private String mName;
}
