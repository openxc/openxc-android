package com.openxc.messages.streamers;

import com.openxc.messages.VehicleMessage;
import com.openxc.sources.SourceLogger;

public abstract class VehicleMessageStreamer {
    public abstract VehicleMessage parseNextMessage();
    public abstract byte[] serializeForStream(VehicleMessage message);

    private final static String TAG = "BytestreamBuffer";
    private final static int STATS_LOG_FREQUENCY_KB = 128;

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
        mBytesReceived += length;
        logTransferStats();
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
