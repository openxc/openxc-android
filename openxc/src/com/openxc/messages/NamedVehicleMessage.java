package com.openxc.messages;

import java.util.Map;

import android.os.Parcel;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;

public class NamedVehicleMessage extends VehicleMessage {
    public static final String NAME_KEY = "name";

    private String mName;

    public NamedVehicleMessage(String name) {
        this(name, null);
    }

    public NamedVehicleMessage(Map<String, Object> values) {
        super(values);
        // TODO pop name out of values
    }

    public NamedVehicleMessage(String name, Map<String, Object> values) {
        super(values);
        mName = name;
    }

    public NamedVehicleMessage(Long timestamp, String name, Map<String, Object> values) {
        super(timestamp, values);
        mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !super.equals(obj)) {
            return false;
        }

        final NamedVehicleMessage other = (NamedVehicleMessage) obj;
        return super.equals(other) && mName.equals(other.mName);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(getName());
    }

    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mName = in.readString();
    }

    private NamedVehicleMessage(Parcel in)
            throws UnrecognizedMeasurementTypeException {
        this();
        readFromParcel(in);
    }

    protected NamedVehicleMessage() { }
}
