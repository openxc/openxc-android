package com.openxc.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.openxc.remote.RawMeasurement;
import com.openxc.util.FileOpener;

/**
 * Record raw vehicle measurements to a file as JSON.
 *
 * This data sink is a simple passthrough that records every raw vehicle
 * measurement as it arrives to a file on the device. It splits the stream to a
 * different file every hour.
 */
public class FileRecorderSink extends BaseVehicleDataSink {
    private final static String TAG = "FileRecorderSink";
    private static SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("yyyy-MM-dd-HH");
    private static DecimalFormat sTimestampFormatter =
            new DecimalFormat("##########.000000");

    private BufferedWriter mWriter;
    private Date mLastFileCreated;
    private FileOpener mFileOpener;

    public FileRecorderSink(FileOpener fileOpener) throws DataSinkException {
        mFileOpener = fileOpener;
        try {
            openTimestampedFile();
        } catch(IOException e) {
            throw new DataSinkException("Unable to open file for recording", e);
        }
    }

    /**
     * Record a message to a file, selected by the current time.
     */
    public synchronized void receive(RawMeasurement measurement)
            throws DataSinkException {
        double timestamp = System.currentTimeMillis() / 1000.0;
        String timestampString = sTimestampFormatter.format(timestamp);
        if(mWriter != null) {
            if((new Date()).getHours() != mLastFileCreated.getHours()) {
                // flip to a new file every hour
                try {
                    openTimestampedFile();
                } catch(IOException e) {
                    throw new DataSinkException("Unable to open file nfor recording",
                            e);
                }
            }

            try {
                mWriter.write(timestampString + ": " + measurement.serialize());
                mWriter.newLine();
            } catch(IOException e) {
                Log.w(TAG, "Unable to write measurement to file", e);
            }
        } else {
            Log.w(TAG, "No valid writer - not recording trace line");
        }
    }

    public synchronized void stop() {
        if(mWriter != null) {
            try {
                mWriter.close();
            } catch(IOException e) {
                Log.w(TAG, "Unable to close output file", e);
            }
            mWriter = null;
        }
        Log.i(TAG, "Shutting down");
    }

    public synchronized boolean isRunning() {
        return mWriter != null;
    }

    public synchronized void flush() {
        if(isRunning()) {
            try {
                mWriter.flush();
            } catch(IOException e) {
                Log.w(TAG, "Unable to flush writer", e);
            }
        }
    }

    public static boolean validatePath(String path) {
        if(path == null) {
            Log.w(TAG, "Recording path not set (it's " + path + ")");
            return false;
        }

        try {
            URI uri = new URI(path);
            return uri.isAbsolute();
        } catch(java.net.URISyntaxException e) {
            return false;
        }
    }

    private synchronized void openTimestampedFile() throws IOException {
        mLastFileCreated = new Date();
        String filename = sDateFormatter.format(mLastFileCreated) + ".json";
        mWriter = mFileOpener.openForWriting(filename);
        Log.i(TAG, "Opened trace file " + filename + " for writing");
    }
}
