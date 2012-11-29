package com.openxc.sources;

import java.util.concurrent.TimeUnit;

import android.util.Log;

public class SourceLogger {
    public static void logTransferStats(final String tag,
            final long startTime, final long endTime,
            final double bytesReceived) {
        double kilobytesTransferred = bytesReceived / 1000.0;
        long elapsedTime = TimeUnit.SECONDS.convert(
            System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        Log.i(tag, "Transferred " + kilobytesTransferred + " KB in "
            + elapsedTime + " seconds at an average of " +
            kilobytesTransferred / elapsedTime + " KB/s");
    }

}
