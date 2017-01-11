package com.openxc.messages.formatters;

import java.io.InputStream;

import com.google.protobuf.MessageLite;
import com.openxc.messages.SerializationException;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.binary.BinaryDeserializer;
import com.openxc.messages.formatters.binary.BinarySerializer;

/**
 * A formatter for serializing and deserializing Protocol Buffers, i.e. the
 * OpenXC binary message format.
 */
public class BinaryFormatter {
    /**
     * Deserialize a single vehicle message from the input stream.
     *
     * @param data The stream that should include protobuf-encoded vehicle
     *  messages.
     * @throws UnrecognizedMessageTypeException if a message could not be
     *  deserialized.
     * @return the deserialized VehicleMessage
     */
    public static VehicleMessage deserialize(InputStream data)
            throws UnrecognizedMessageTypeException {
        return BinaryDeserializer.deserialize(data);
    }

    /**
     * Serialize a VehicleMessage into a byte array.
     *
     * @param message the VehicleMessage to serialize.
     * @throws SerializationException if there was an error with serializing the
     *      message.
     * @return the message serialized to bytes.
     */
    public static byte[] serialize(VehicleMessage message)
            throws SerializationException {
        return preSerialize(message).toByteArray();
    }

    /**
     * Serialize a VehicleMessage into an intermediate protobuf object.
     *
     * This used to write delimited, serialized messages to a stream for the
     * binary format
     */
    public static MessageLite preSerialize(VehicleMessage message)
            throws SerializationException {
        return BinarySerializer.preSerialize(message);
    }
}
