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

    private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream();;       // filled by receive(...) and empties previous contents
                                                                                // mBuffer (Persists) is next bytes to be translated
    private int inSyncCount = 0;   // Number of messages that have been insync
    private int syncErrors = 0;     // Number of errors encountered

    // returns the number of bytes read off of the InputStream
    private int swallowSyncMessage(InputStream input, int size, int bytesRemaining) throws IOException {
        int bytesRead = 0;

        final int SYNC_MSG_LEN = 7;
        if ((size == SYNC_MSG_LEN) && (bytesRemaining >= size)) {
            input.mark(SYNC_MSG_LEN + 1);
            byte[] buffer = new byte[SYNC_MSG_LEN];
            int numRead = input.read(buffer, 0, SYNC_MSG_LEN);
            bytesRead += SYNC_MSG_LEN;
            if (numRead == SYNC_MSG_LEN) {
                if ((buffer[0] == 'S') && (buffer[1] == 'Y') && (buffer[2] == 'N') && (buffer[3] == 'C') &&
                        (buffer[4] == 'M') && (buffer[5] == 'S') && (buffer[6] == 'G')) {  // Sync Found
                } else {  // Not a sync message
                    input.reset();      // Reset the stream back
                    bytesRead = 0;
                }
            }
        }
        return bytesRead;
    }

    @Override
    public VehicleMessage parseNextMessage() {

        // This block of code is debug output.  Is this DUPLICATE Functionality to dumpToLog?
//        byte[] gBuf = mBuffer.toByteArray();
//        String gRes = "";
//        for(int cnt = 0; cnt<gBuf.length; cnt++) {
//            gRes += Integer.toHexString(gBuf[cnt]) + " ";
//        }
//        Log.e("=== BinaryStreamer", gRes);

        // TODO we could move this to a separate thread and use a
        // piped input stream, where it would block on the
        // bytestream until more data was available - but that might
        // be messy rather than this approach, which is just
        // inefficient
        InputStream input = new ByteArrayInputStream(mBuffer.toByteArray());
        VehicleMessage message = null;
        int bytesRemaining = mBuffer.size();        // Number of bytes in the buffer, we are going to see if we have enough for our next Protobuf message

        boolean finished = false;
        while(!finished) {
            try {
                int firstByte = input.read();   // Read a single byte off of the buffer and increment position in stream
                bytesRemaining -= 1;
                if (firstByte == -1) {
                    return null;
                }

                int size = CodedInputStream.readRawVarint32(firstByte, input);  // Decodes length byte and determines if it is a special message (>127 bytes)

                //
                // Look for a SYNC Message! 1/11/2022 gja
                //
                int syncMessageSize = swallowSyncMessage(input, size, bytesRemaining);
                bytesRemaining -= syncMessageSize;
                if (bytesRemaining <= 0) {
                    return null;
                } else if (syncMessageSize > 0) {
                    // So read start of next message if any
                    firstByte = input.read();   // Read a single byte off of the buffer and increment position in stream
                    bytesRemaining -= 1;
                    if (firstByte == -1) {
                        return null;
                    }
                    size = CodedInputStream.readRawVarint32(firstByte, input);  // Decodes length byte and determines if it is a special message (>127 bytes)
                }


                if(size > 0 && bytesRemaining >= size) {
                    message = BinaryFormatter.deserialize(ByteStreams.limit(input, size));  // We have enough bytes to get our next message so get it!
                    bytesRemaining -= size;
                    // If message is invalid an exception will be triggered below
                    inSyncCount++;
                } else {
                    // Not deserialized "size" is length of the current Protobuf message that is being looked and is
                    // probably still incomplete by this point. "bytesRemaining" is the number of unparsed bytes
                    // left in the BLE buffer
                    if ((size > 0) && (bytesRemaining < size)) {
                        // Buffer does not hold enough data to finish off a complete message, we need to wait for
                        // another section of data to come through.  No need to backup/reverse a byte so that firstByte
                        // will be read correctly next time in since a fresh input will be composed from the mBuffer next time through
                        finished = true;
                    } else {
                        inSyncCount = 0;
                        syncErrors++;
                    }
                }
            } catch(IOException e) {
                Log.e(TAG, "Unexpected error copying buffers");
            } catch(UnrecognizedMessageTypeException e) {
                Log.e(TAG, "Deserialized protobuf had was unrecognized message type", e);
            }

            if (message != null) finished = true;
        } // end while(message == null)

        if(message != null) {
            mBuffer = new ByteArrayOutputStream();
            lastMessage = message;
            try {
                if (input.available() != 0) {
                    IOUtils.copy(input, mBuffer);   // Prepare mBuffer for next message by coping unused/remaining bytes to be read for next time
                }
            } catch (IOException e) {
                Log.e(TAG, "Unexpected error copying buffers");
            }
        }

        // mBuffer.size() is the number of left over bytes - next buffer fill we will see if the message still has
        // enough to form a vehicle message

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


    public int getBufferSize() {
        return mBuffer.size();
    }

    //
    // receive()
    // mBuffer.size() -> accumulated contents of the BLE buffer mBuffer.write() will increase the amount of data
    //                            in this buffer usually by up to 20 bytes
    // bytes -> raw data that was just read from BLE to be buffered
    // length -> number of bytes to be added
    //
    @Override
    public void receive(byte[] bytes, int length) {
        super.receive(bytes, length);
        mBuffer.write(bytes, 0, length);  // Write incoming bytes starting at bytes[0] with length "length" to end of mBuffer
        dumpToLog(bytes, length);
    }
}
