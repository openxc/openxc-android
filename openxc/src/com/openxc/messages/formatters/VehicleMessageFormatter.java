package com.openxc.messages.formatters;

import java.io.InputStream;

import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.VehicleMessage;

public interface VehicleMessageFormatter {
    public VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMeasurementTypeException;
    public byte[] serialize(VehicleMessage message);
}
