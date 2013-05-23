package com.openxc.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.util.Log;

import com.openxc.remote.RawMeasurement;
import com.openxc.util.FileOpener;

/**
 * Record raw vehicle measurements to a file as JSON.
 *
 * This data sink is a simple passthrough that records every raw vehicle
 * measurement as it arrives to a file on the device. It uses a heuristic to
 * detect different "trips" in the vehicle, and splits the recorded trace by
 * trip.
 *
 * The heuristic is very simple: if we haven't received any new data in a while,
 * consider the previous trip to have ended. When activity resumes, start a new
 * trip.
 */
public class FileRecorderSink extends BaseVehicleDataSink {
    private final static String TAG = "FileRecorderSink";
    private final static int INTER_TRIP_THRESHOLD_MINUTES = 5;
    private static SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private FileOpener mFileOpener;
    private BufferedWriter mWriter;
    private Calendar mLastMessageReceived;

    public FileRecorderSink(FileOpener fileOpener) throws DataSinkException {
        mFileOpener = fileOpener;
    }

    public synchronized boolean receive(RawMeasurement measurement)
            throws DataSinkException {
        if(mLastMessageReceived == null ||
                    GregorianCalendar.getInstance().getTimeInMillis()
                    - mLastMessageReceived.getTimeInMillis()
                > INTER_TRIP_THRESHOLD_MINUTES * 60 * 1000) {
            Log.i(TAG, "Detected a new trip, splitting recorded trace file");
            try {
                openTimestampedFile();
            } catch(IOException e) {
                throw new DataSinkException(
                        "Unable to open file for recording", e);
            }
        }

        if(mWriter == null) {
            throw new DataSinkException(
                    "No valid writer - not recording trace line");
        }

        mLastMessageReceived = GregorianCalendar.getInstance();
        try {
            mWriter.write(measurement.serialize());
            mWriter.newLine();
        } catch(IOException e) {
            Log.w(TAG, "Unable to write measurement to file", e);
            return false;
        }
        return true;
    }

    public synchronized void stop() {
        close();
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

    private synchronized void close() {
        if(mWriter != null) {
            try {
                mWriter.close();
            } catch(IOException e) {
                Log.w(TAG, "Unable to close output file", e);
            }
            mWriter = null;
        }
    }

    private synchronized Calendar openTimestampedFile() throws IOException {
        Calendar calendar = GregorianCalendar.getInstance();
        String filename = sDateFormatter.format(
                calendar.getTime()) + ".json";
        if(mWriter != null) {
            close();
        }
        mWriter = mFileOpener.openForWriting(filename);
        Log.i(TAG, "Opened trace file " + filename + " for writing");
        return calendar;
    }
}
