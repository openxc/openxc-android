package com.openxc.messages.streamers;

import com.openxc.messages.SerializationException;
import com.openxc.messages.VehicleMessage;
import com.openxc.sources.SourceLogger;

/**
 * A base class for VehicleMessage streamers that defines the interface and
 * handles counting the amount of data received.
 */
public abstract class VehicleMessageStreamer {
    /**
     * Deserialize and return the next messages from the internally buffered
     * stream.
     *
     * @return the next deserialized VehicleMessage.
     */
    public abstract VehicleMessage parseNextMessage();

    /**
     * Serialize the message and insert any required delimiters for insertion
     * into a message stream.
     *
     * @param message the message to serialize.
     * @throws SerializationException if the message cannot be serialized.
     */
    public abstract byte[] serializeForStream(VehicleMessage message)
            throws SerializationException;

    private final static String TAG = "VehicleMessageStreamer";
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
            SourceLogger.logTransferStats(TAG, mLastLoggedStatsTime,
                    mBytesReceived - mLastLoggedTransferStatsAtByte);
            mLastLoggedTransferStatsAtByte = mBytesReceived;
            mLastLoggedStatsTime = System.nanoTime();
        }
    }
}
