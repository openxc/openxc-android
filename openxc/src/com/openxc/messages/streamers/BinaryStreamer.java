package com.openxc.messages.streamers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.protobuf.CodedInputStream;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.BinaryFormatter;

public class BinaryStreamer extends VehicleMessageStreamer {
    private static String TAG = "JsonStreamer";

    private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream();

    public VehicleMessage parseNextMessage() {
        // TODO we could move this to a separate thread and use a
        // piped input stream, where it would block on the
        // bytestream until more data was available - but that might
        // be messy rather than this approach, which is just
        // inefficient
        InputStream input = new ByteArrayInputStream(mBuffer.toByteArray());
        VehicleMessage message = null;
        while(message == null) {
            try {
                int firstByte = input.read();
                if (firstByte == -1) {
                    return null;
                }

                int size = CodedInputStream.readRawVarint32(firstByte, input);
                if(size > 0) {
                    message = new BinaryFormatter().deserialize(
                            ByteStreams.limit(input, size));
                }

                if(message != null) {
                    mBuffer = new ByteArrayOutputStream();
                    IOUtils.copy(input, mBuffer);
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to deserialize protobuf", e);
            } catch(UnrecognizedMessageTypeException e) {
                Log.w(TAG, "Deserialized protobuf had was unrecognized message type", e);
            }

        }
        return message;
    }

    public byte[] serializeForStream(VehicleMessage message) {
        // TODO
        return null;
    }

    @Override
    public void receive(byte[] bytes, int length) {
        super.receive(bytes, length);
        mBuffer.write(bytes, 0, length);
    }
}
