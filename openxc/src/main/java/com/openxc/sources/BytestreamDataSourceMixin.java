package com.openxc.sources;

import android.util.Log;

/**
 * A "mixin" of sorts to be used with object composition, this contains
 * functionality common to data sources that received streams of bytes.
 */
public class BytestreamDataSourceMixin {
    private final static String TAG = "BytestreamDataSourceMixin";
    private StringBuilder mBuffer = new StringBuilder(512);
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
        // Creating a new String object for each message causes the
        // GC to go a little crazy, but I don't see another obvious way
        // of converting the byte[] to something the StringBuilder can
        // accept (either char[] or String). See #151.
        String data = new String(bytes, 0, length);
        mBuffer.append(data);
        mBytesReceived += length;

        // log the transfer stats roughly every 1MB
        if(mBytesReceived > mLastLoggedTransferStatsAtByte + 1024 * 1024) {
            mLastLoggedTransferStatsAtByte = mBytesReceived;
            SourceLogger.logTransferStats(TAG, mStartTime, System.nanoTime(),
                    mBytesReceived);
        }
    }

    /**
     * Parse the current byte buffer to find messages. Any messages found in the
     * buffer are removed and returned.
     *
     * @returns A list of messages parsed and subsequently removed from the
     * buffer, if any.
     */
    public String[] parse() {
        String[] records = mBuffer.toString().split("\n");

        if(records.length > 1) {
            String newBuffer = records[records.length - 1];
            mBuffer = new StringBuilder(newBuffer.length() * 2);
            mBuffer.append(newBuffer);
        }
        return records;
    }
}
