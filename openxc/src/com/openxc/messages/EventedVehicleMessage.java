package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class EventedVehicleMessage extends SimpleVehicleMessage {
    public static final String EVENT_KEY = "event";

    //TODO this can't be the right type for this...
    private String mEvent; 

    // TODO should make this a generic that sets type for the value
    public EventedVehicleMessage(String name, Object value, String event) {
        super(name, value);
        mEvent = event;
        put(EVENT_KEY, event);
    }

    public EventedVehicleMessage(Long timestamp, String name, Object value, String event) {
        super(timestamp, name, value);
        mEvent = event;
        put(EVENT_KEY, event);
    }

    public EventedVehicleMessage(Map<String, Object> values) {
        //TODO could be better, but works
        //must use awful-readability-having ternary expression because constructor must be first statement
        this(values.containsKey(TIMESTAMP_KEY) ? ((Double) values.get(TIMESTAMP_KEY)).longValue() : null, 
                (String) values.get(NAME_KEY), values.get(VALUE_KEY), (String)values.get(EVENT_KEY));        
    }
    
    public String getEvent() {
        return mEvent;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getEvent());
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mEvent = in.readString();
    }

    private EventedVehicleMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    private EventedVehicleMessage() { }
}
