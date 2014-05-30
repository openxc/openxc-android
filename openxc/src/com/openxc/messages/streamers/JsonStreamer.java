package com.openxc.messages.streamers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;

public class JsonStreamer extends VehicleMessageStreamer {
    private final static String DELIMITER = "\u0000";

    private StringBuffer mBuffer = new StringBuffer();
    private JsonFormatter mFormatter = new JsonFormatter();

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

    public VehicleMessage parseNextMessage() {
        String line = readLine();
        if(line != null) {
            try {
                return mFormatter.deserialize(new ByteArrayInputStream(
                            line.getBytes(StandardCharsets.UTF_8)));
            } catch(UnrecognizedMeasurementTypeException e) {
            }
        }
        return null;
    }

    @Override
    public void receive(byte[] bytes, int length) {
        super.receive(bytes, length);
        mBuffer.append(new String(bytes));
    }

    public byte[] serializeForStream(VehicleMessage message) {
        return (mFormatter.serialize(message) + DELIMITER).getBytes();
    }

    /**
     * Parse the current byte buffer to find messages.
     *
     * Any messages found in the
     * buffer are removed and returned.
     *
     * @return A list of messages parsed and subsequently removed from the
     *      buffer, if any.
     */
    private String readLine() {
        int delimiterIndex = mBuffer.indexOf(DELIMITER);
        String line = null;
        if(delimiterIndex != -1) {
            line = mBuffer.substring(0, delimiterIndex + 1);
            mBuffer.delete(0, delimiterIndex + 1);
        }
        return line;
    }

    // TODO really don't need this but we have unit tests for it and I don't
    // want to just throw those away - need to pull out the string buffering
    // part from this class and test that separately
    public List<String> readLines() {
        List<String> lines = new ArrayList<String>();
        String line = null;
        while((line = readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
}
