package com.openxc.messages.formatters;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.openxc.BinaryMessages;
import com.openxc.messages.CanMessage;
import com.openxc.messages.Command;
import com.openxc.messages.Command.CommandType;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.EventedSimpleVehicleMessage;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SerializationException;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.binary.BinaryDeserializer;
import com.openxc.messages.formatters.binary.BinarySerializer;

public class BinaryFormatter {
    private final static String TAG = "BinaryFormatter";

    public static VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        return BinaryDeserializer.deserialize(data);
    }

    public static byte[] serialize(VehicleMessage message)
            throws SerializationException {
        return BinarySerializer.serialize(message);
    }
}
