package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class SimpleVehicleMessage extends NamedVehicleMessage {
    public static final String VALUE_KEY = "value";

    private Object mValue;

    // TODO should make this a generic that sets type for the value
    public SimpleVehicleMessage(String name, Object value) {
        super(name, null);
        mValue = value;
        put(VALUE_KEY, value);
    }

    public SimpleVehicleMessage(Long timestamp, String name, Object value) {
        super(timestamp, name, null);
        mValue = value;
        put(VALUE_KEY, value);
    }

    public SimpleVehicleMessage(Map<String, Object> values) {
    	//TODO could be better, but works
    	//must use awful-readability-having ternary expression because constructor must be first statement
        this(values.containsKey(TIMESTAMP_KEY) ? ((Double) values.get(TIMESTAMP_KEY)).longValue() : null, 
        		(String) values.get(NAME_KEY), values.get(VALUE_KEY));        
    }

    public Object getValue() {
        return mValue;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        // TODO This is going to write out the value twice, I think.
        out.writeValue(getValue());
    }

    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mValue = in.readValue(null);
    }

    private SimpleVehicleMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    protected SimpleVehicleMessage() { }
}
