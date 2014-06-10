package com.openxc.messages.formatters;

import java.io.InputStream;

import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;

public interface VehicleMessageFormatter {
    public VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException;
    public byte[] serialize(VehicleMessage message);
}
