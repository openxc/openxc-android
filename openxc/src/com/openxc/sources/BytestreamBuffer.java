package com.openxc.sources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.io.ByteStreams;
import com.google.protobuf.CodedInputStream;
import com.openxc.BinaryMessages;

/**
 * A "mixin" of sorts to be used with object composition, this contains
 * functionality common to data sources that received streams of bytes.
 */
public class BytestreamBuffer {
    private final static String TAG = "BytestreamBuffer";
    private final static int STATS_LOG_FREQUENCY_KB = 128;

    private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream();
    private double mBytesReceived = 0;
    private double mLastLoggedTransferStatsAtByte = 0;
    private long mLastLoggedStatsTime = System.nanoTime();

    /**
     * Add additional bytes to the buffer from the data source.
     *
     * @param bytes an array of bytes received from the interface.
     * @param length number of bytes received, and thus the amount that should
     *      be read from the array.
     */
    public void receive(byte[] bytes, int length) {
        mBuffer.write(bytes, 0, length);
        mBytesReceived += length;

        logTransferStats();
    }

    /**
     * Parse the current byte buffer to find messages. Any messages found in the
     * buffer are removed and returned.
     *
     * @return A list of messages parsed and subsequently removed from the
     *      buffer, if any.
     */
    public List<String> readLines() {
        List<String> result;
        String bufferedString = mBuffer.toString();
        if(bufferedString.indexOf("\u0000") != -1) {
            String[] records = bufferedString.toString().split("\u0000", -1);

            mBuffer = new ByteArrayOutputStream();
            result = Arrays.asList(records).subList(0, records.length - 1);

            // Preserve any remaining, trailing incomplete messages in the
            // buffer
            if(records[records.length - 1].length() > 0) {
                byte[] remainingData = records[records.length - 1].getBytes();
                mBuffer.write(remainingData, 0, remainingData.length);
            }
        } else {
            result = new ArrayList<String>();
        }

        return result;
    }

    private static boolean validateProtobuf(BinaryMessages.VehicleMessage message) {
        return (message.hasTranslatedMessage() &&
                        message.getTranslatedMessage().hasName() && (
                            message.getTranslatedMessage().hasNumericValue() ||
                            message.getTranslatedMessage().hasStringValue() ||
                            message.getTranslatedMessage().hasBooleanValue()))
            // TODO raw messages aren't supported upstream in the library at the
            // moment so we forcefully reject it here
                || (false && message.hasRawMessage() &&
                        message.getRawMessage().hasMessageId() &&
                        message.getRawMessage().hasData());
    }

    public BinaryMessages.VehicleMessage readBinaryMessage() {
        // TODO we could move this to a separate thread and use a
        // piped input stream, where it would block on the
        // bytestream until more data was available - but that might
        // be messy rather than this approach, which is just
        // inefficient
        InputStream input = new ByteArrayInputStream(
                mBuffer.toByteArray());
        BinaryMessages.VehicleMessage message = null;
        while(message == null) {
            try {
                int firstByte = input.read();
                if (firstByte == -1) {
                    return null;
                }

                int size = CodedInputStream.readRawVarint32(firstByte, input);
                if(size > 0) {
                    message = BinaryMessages.VehicleMessage.parseFrom(
                            ByteStreams.limit(input, size));
                }

                if(message != null && validateProtobuf(message)) {
                    mBuffer = new ByteArrayOutputStream();
                    int remainingByte;
                    while((remainingByte = input.read()) != -1) {
                        mBuffer.write(remainingByte);
                    }
                } else {
                    message = null;
                }
            } catch(IOException e) { }

        }
        return message;
    }

    /**
     * Return true if the buffer *most likely* contains JSON (as opposed to a
     * protobuf).
     */
    public boolean containsJson() {
        return CharMatcher.ASCII
            // We need to allow the \u0000 delimiter for JSON messages, so we
            // can't use the JAVA_ISO_CONTROL character set and must build the
            // range manually (minus \u0000)
            .and(CharMatcher.inRange('\u0001', '\u001f').negate())
            .and(CharMatcher.inRange('\u007f', '\u009f').negate())
            .and(CharMatcher.ASCII)
            .matchesAllOf(mBuffer.toString());
    }

    private void logTransferStats() {
        if(mBytesReceived > mLastLoggedTransferStatsAtByte +
                STATS_LOG_FREQUENCY_KB * 1024) {
            SourceLogger.logTransferStats(TAG, mLastLoggedStatsTime, System.nanoTime(),
                    mBytesReceived - mLastLoggedTransferStatsAtByte);
            mLastLoggedTransferStatsAtByte = mBytesReceived;
            mLastLoggedStatsTime = System.nanoTime();
        }
    }

}
