package com.openxc.messages.formatters;

import java.io.InputStream;

import com.google.protobuf.MessageLite;
import com.openxc.messages.SerializationException;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.binary.BinaryDeserializer;
import com.openxc.messages.formatters.binary.BinarySerializer;

public class BinaryFormatter {
    public static VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        return BinaryDeserializer.deserialize(data);
    }

    public static byte[] serialize(VehicleMessage message)
            throws SerializationException {
        return preSerialize(message).toByteArray();
    }

    public static MessageLite preSerialize(VehicleMessage message)
            throws SerializationException {
        return BinarySerializer.preSerialize(message);
    }
}
