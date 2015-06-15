package com.openxc.sources;

import java.util.concurrent.TimeUnit;

import android.util.Log;

public class SourceLogger {
    /**
     * Log data transfer statistics to the Android log.
     *
     * @param tag the Android log tag to use.
     * @param startTime the starting time for the stats calculation in
     * nanoseconds.
     * @param bytesReceived the number of bytes received over the given period
     * of time
     */
    public static void logTransferStats(final String tag,
            final long startTime, final double bytesReceived) {
        double kilobytesTransferred = bytesReceived / 1024.0;
        long elapsedTime = TimeUnit.SECONDS.convert(
            System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        Log.i(tag, "Transferred " + kilobytesTransferred + " KB in the last "
            + elapsedTime + " seconds at an average of " +
            kilobytesTransferred / elapsedTime + " KB/s");
    }

}
