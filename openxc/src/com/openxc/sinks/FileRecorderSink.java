package com.openxc.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
            new SimpleDateFormat("yyyy-MM-dd-HH", Locale.US);

    private FileOpener mFileOpener;
    private BufferedWriter mWriter;
    private Calendar mLastFileCreated;

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
    public synchronized boolean receive(RawMeasurement measurement)
            throws DataSinkException {
        if(mWriter != null) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(new Date());
            if(calendar.get(Calendar.HOUR) !=
                    mLastFileCreated.get(Calendar.HOUR)) {
                try {
                    openTimestampedFile();
                } catch(IOException e) {
                    throw new DataSinkException(
                            "Unable to open file for recording", e);
                }
            }

            try {
                mWriter.write(measurement.serialize());
                mWriter.newLine();
            } catch(IOException e) {
                Log.w(TAG, "Unable to write measurement to file", e);
                return false;
            }
        } else {
            Log.w(TAG, "No valid writer - not recording trace line");
            return false;
        }
        return true;
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

    public synchronized void flush() {
        if(mWriter != null) {
            try {
                mWriter.flush();
            } catch(IOException e) {
                Log.w(TAG, "Unable to flush writer", e);
            }
        }
    }

    private synchronized void openTimestampedFile() throws IOException {
        mLastFileCreated = GregorianCalendar.getInstance();
        mLastFileCreated.setTime(new Date());
        String filename = sDateFormatter.format(
                mLastFileCreated.getTime()) + ".json";
        mWriter = mFileOpener.openForWriting(filename);
        Log.i(TAG, "Opened trace file " + filename + " for writing");
    }
}
