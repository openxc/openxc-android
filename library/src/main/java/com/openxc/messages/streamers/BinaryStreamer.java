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
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.MultiFrameResponse;
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

    private static final boolean DEBUG = false;
    private static VehicleMessage lastMessage = null;

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
            lastMessage = message;
            try {
                IOUtils.copy(input, mBuffer);
            } catch(IOException e) {
                Log.e(TAG, "Unexpected error copying buffers");
            }
        }
        return message;
    }

    @Override
    public VehicleMessage parseMessage(String line) {

        if (lastMessage instanceof MultiFrameResponse) {
            DiagnosticResponse response = new DiagnosticResponse(((MultiFrameResponse) lastMessage).getBus(), ((MultiFrameResponse) lastMessage).getMessageId(), ((MultiFrameResponse) lastMessage).getMode());

            if (line.length()>2) {

                byte[] binaryPayload = new byte[(line.length()-2)/2];

                // Turn string into binary data
                for(int index=0, cnt=2; cnt<line.length(); index++, cnt+=2) {  // Skip first 2 chars which are "0x"
                    char first = line.charAt(cnt);
                    char second = '0';

                    if ((cnt + 1) < line.length()) {
                        second = line.charAt(cnt + 1);
                    }

                    int value = 0;

                    if ((first >= 'a') && (first <= 'f')) {
                        value = (first - 'a' + 10) * 16;
                    } else if ((first >= 'A') && (first <= 'F')) {
                        value = (first - 'A' + 10) * 16;
                    } else if ((first >= '0') && (first <= '9')) {
                        value = (first - '0') * 16;
                    }

                    if ((second >= 'a') && (second <= 'f')) {
                        value += (second - 'a' + 10);
                    } else if ((second >= 'A') && (second <= 'F')) {
                        value += (second - 'A' + 10);
                    } else if ((second >= '0') && (second <= '9')) {
                        value += (second - '0');
                    }

                    binaryPayload[index] = (byte)value;
                }

                response.setPayload(binaryPayload);
            }
            return response;
        }
        return null;
    }

    @Override
    public String getRawMessage() {
        return null;
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

    private static final int NUMPERROW = 8;
    public static void dumpToLog(byte[] bytes, int length) {
        if (DEBUG == false) return;
        String lineout;
        for(int row=0; row <= (length-1) / NUMPERROW; row++) {
            lineout = "";
            for(int col=0; (col < NUMPERROW) && (((row*NUMPERROW) + col) < length); col++) {
                lineout += String.format("%1$02X ", bytes[row*NUMPERROW+col]);
            }
            Log.e(TAG, lineout);
        }
    }

    @Override
    public void receive(byte[] bytes, int length) {
        super.receive(bytes, length);
        mBuffer.write(bytes, 0, length);
        dumpToLog(bytes, length);
    }
}
