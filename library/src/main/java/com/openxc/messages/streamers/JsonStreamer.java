package com.openxc.messages.streamers;

import android.util.Log;

import com.google.common.base.CharMatcher;
import com.openxc.messages.UnrecognizedMessageTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;

/**
 * A class to deserialize and serialize JSON-formatted vehicle messages from
 * byte streams.
 *
 * The JsonStreamer wraps the JsonFormatter and handles messages delimiting.
 * It uses a \u0000 delimiter as specified by the OpenXC message format.
 *
 * Unlike the JsonFormatter, the JsonStreamer is not stateless. It maintains
 * an internal buffer of bytes so that if partial messages is received it can
 * eventually receive an parse the entire thing.
 */
public class JsonStreamer extends VehicleMessageStreamer {
    private static String TAG = "JsonStreamer";
    private final static String DELIMITER = "\u0000";

    private StringBuffer mBuffer = new StringBuffer();

    /**
     * Return true if the buffer *most likely* contains JSON (as opposed to a
     * protobuf).
     */
    public static boolean containsJson(String buffer) {
        return CharMatcher.ASCII
            // We need to allow the \u0000 delimiter for JSON messages, so we
            // can't use the JAVA_ISO_CONTROL character set and must build the
            // range manually (minus \u0000)
            .and(CharMatcher.inRange('\u0001', '\u001f').negate())
            .and(CharMatcher.inRange('\u007f', '\u009f').negate())
            .and(CharMatcher.ASCII)
            .matchesAllOf(buffer);
    }

    @Override
    public VehicleMessage parseNextMessage() {
        String line = readToDelimiter();
        if(line != null) {
            try {
                return JsonFormatter.deserialize(line);
            } catch(UnrecognizedMessageTypeException e) {
                Log.w(TAG, "Unable to deserialize JSON", e);
            }
        }
        return null;
    }

    @Override
    public void receive(byte[] bytes, int length) {
        super.receive(bytes, length);
        // Creating a new String object for each message causes the
        // GC to go a little crazy, but I don't see another obvious way
        // of converting the byte[] to something the StringBuilder can
        // accept (either char[] or String). See #151.
        mBuffer.append(new String(bytes, 0, length));
    }

    @Override
    public byte[] serializeForStream(VehicleMessage message) {
        return (JsonFormatter.serialize(message) + DELIMITER).getBytes();
    }

    /**
     * Parse the current byte buffer to find the next potential message.
     *
     * The first potential serialized message data in the buffer is removed and
     * returned. Any delimiters at the start of the buffer will be cleared.
     *
     * @return A potential serialized JSON message or null if none found.
     */
    private String readToDelimiter() {
        String line = null;
        while(line == null || line.isEmpty()) {
            int delimiterIndex = mBuffer.indexOf(DELIMITER);
            if(delimiterIndex != -1) {
                line = mBuffer.substring(0, delimiterIndex);
                mBuffer.delete(0, delimiterIndex + 1);
            } else {
                line = null;
                break;
            }
        }
        return line;
    }
}
