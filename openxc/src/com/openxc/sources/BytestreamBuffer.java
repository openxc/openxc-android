package com.openxc.sources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

/**
 * A "mixin" of sorts to be used with object composition, this contains
 * functionality common to data sources that received streams of bytes.
 */
public class BytestreamBuffer {
    private final static String TAG = "BytestreamBuffer";

    private OutputStream mBuffer = new ByteArrayOutputStream();
    private double mBytesReceived = 0;
    private double mLastLoggedTransferStatsAtByte = 0;
    private final long mStartTime = System.nanoTime();

    /**
     * Add additional bytes to the buffer from the data source.
     *
     * @param bytes an array of bytes received from the interface.
     * @param length number of bytes received, and thus the amount that should
     *      be read from the array.
     */
    public void receive(byte[] bytes, int length) {
        try {
            mBuffer.write(bytes, 0, length);
            mBytesReceived += length;
        } catch(IOException e) {
            Log.w(TAG, "Unable to buffer fresh bytes", e);
        }

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
        if(bufferedString.indexOf("\n") != -1) {
            String[] records = bufferedString.toString().split("\n", -1);

            mBuffer = new ByteArrayOutputStream();
            result = Arrays.asList(records).subList(0, records.length - 1);

            // Preserve any remaining, trailing incomplete messages in the
            // buffer
            if(records[records.length - 1].length() > 0) {
                byte[] remainingData = records[records.length - 1].getBytes();
                try {
                    mBuffer.write(remainingData, 0, remainingData.length);
                } catch(IOException e) {
                    Log.w(TAG, "Unable to preserve remaining data in buffer", e);
                }
            }
        } else {
            result = new ArrayList<String>();
        }

        return result;
    }

    private void logTransferStats() {
        // log the transfer stats roughly every 1MB
        if(mBytesReceived > mLastLoggedTransferStatsAtByte + 1024 * 1024) {
            mLastLoggedTransferStatsAtByte = mBytesReceived;
            SourceLogger.logTransferStats(TAG, mStartTime, System.nanoTime(),
                    mBytesReceived);
        }
    }

}
