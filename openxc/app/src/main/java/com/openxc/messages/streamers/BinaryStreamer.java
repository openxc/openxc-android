package com.openxc.messages.streamers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.MessageLite;
import com.openxc.messages.SerializationException;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.BinaryFormatter;

/**
 * A class to deserialize and serialize binary-formatted vehicle messages from
 * byte streams.
 *
 * The BinaryStreamer wraps the BinaryFormatter and handles messages delimiting.
 * It uses standard message length delimiters as recommended by the protobuf
 * docs and specific in the OpenXC message format.
 *
 * Unlike the BinaryFormatter, the BinaryStreamer is not stateless. It maintains
 * an internal buffer of bytes so that if partial messages is received it can
 * eventually receive an parse the entire thing.
 */
public class BinaryStreamer extends VehicleMessageStreamer {
    private static String TAG = "BinaryStreamer";

    private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream();

    @Override
    public VehicleMessage parseNextMessage() {
        // TODO we could move this to a separate thread and use a
        // piped input stream, where it would block on the
        // bytestream until more data was available - but that might
        // be messy rather than this approach, which is just
        // inefficient
        InputStream input = new ByteArrayInputStream(mBuffer.toByteArray());
        VehicleMessage message = null;
        int bytesRemaining = mBuffer.size();
        while(message == null) {
            try {
                int firstByte = input.read();
                bytesRemaining -= 1;
                if (firstByte == -1) {
                    return null;
                }

                int size = CodedInputStream.readRawVarint32(firstByte, input);
                if(size > 0 && bytesRemaining >= size) {
                    message = BinaryFormatter.deserialize(
                            ByteStreams.limit(input, size));
                } else {
                    break;
                }
            } catch(IOException e) {
                Log.e(TAG, "Unexpected errror copying buffers");
            } catch(UnrecognizedMessageTypeException e) {
                Log.w(TAG, "Deserialized protobuf had was unrecognized message type", e);
            }
        }

        if(message != null) {
            mBuffer = new ByteArrayOutputStream();
            try {
                IOUtils.copy(input, mBuffer);
            } catch(IOException e) {
                Log.e(TAG, "Unexpected error copying buffers");
            }
        }
        return message;
    }

    @Override
    public byte[] serializeForStream(VehicleMessage message)
            throws SerializationException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            MessageLite preSerialized = BinaryFormatter.preSerialize(message);
            preSerialized.writeDelimitedTo(stream);
        } catch(IOException e) {
            throw new SerializationException(
                    "Unable to serialize message to stream", e);
        }
        return stream.toByteArray();
    }

    @Override
    public void receive(byte[] bytes, int length) {
        super.receive(bytes, length);
        mBuffer.write(bytes, 0, length);
    }
}
