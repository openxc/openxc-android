package com.openxc.messages;

import java.util.Map;

public abstract class DiagnosticMessage extends VehicleMessage {
    
	public static final String PID_KEY = "pid";
	public static final String PAYLOAD_KEY = "payload";

	//TODO what's a good default value to give these (here and in the subclasses)? 
	//0 for everything like it is now as the java default doesn't seem good...most necessary 
	//for the optional fields. or does it matter?
	protected int mCanBus;
    protected int mId;
    protected int mMode;
    protected int mPid;
    protected byte[] mPayload;
    
    protected DiagnosticMessage(Map<String, Object> values) {
    	super(values);
    	if (values != null) {
	    	if (values.containsKey(BUS_KEY)) {
	    		mCanBus = (int)values.get(VehicleMessage.BUS_KEY);
	    	}
	    	if (values.containsKey(ID_KEY)) {
	    		mId = (int)values.get(VehicleMessage.ID_KEY);
	    	}
	    	if (values.containsKey(MODE_KEY)) {
	    		mMode = (int)values.get(VehicleMessage.MODE_KEY);
	    	}
	        if (values.containsKey(PID_KEY)) {
	        	mPid = (int)values.get(PID_KEY);
	        } 
	        if (values.containsKey(PAYLOAD_KEY)) {
	        	mPayload = (byte[])values.get(PAYLOAD_KEY);
	        }
    	}
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final DiagnosticMessage other = (DiagnosticMessage) obj;
        return super.equals(other) && (mCanBus == other.mCanBus) && (mId == other.mId)
        		&& (mMode == other.mMode) && (mPid == other.mPid) 
        		&& (mPayload.equals(other.mPayload));
    }

    public int getCanBus() {
    	return mCanBus;
    }
    
    public int getId() {
    	return mId;
    }
    
    public int getMode() {
    	return mMode;
    }
    
    public int getPid() {
    	return mPid;
    }
    
    public byte[] getPayload() {
    	return mPayload;
    }
    
}
