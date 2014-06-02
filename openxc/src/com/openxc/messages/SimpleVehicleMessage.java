package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class SimpleVehicleMessage extends NamedVehicleMessage {
    public static final String VALUE_KEY = "value";

    private Object mValue;

    public SimpleVehicleMessage(Long timestamp, String name, Object value) {
        super(timestamp, name, null);
        mValue = value;
    }

    public SimpleVehicleMessage(String name, Object value) {
        this(null, name, null);
    }

    public SimpleVehicleMessage(Map<String, Object> values) {
        super(values);
        mValue = getValuesMap().remove(VALUE_KEY);
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

    protected SimpleVehicleMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        readFromParcel(in);
    }

    protected SimpleVehicleMessage() { }
}
