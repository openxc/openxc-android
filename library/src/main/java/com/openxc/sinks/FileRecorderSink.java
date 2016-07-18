package com.openxc.sinks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;
import com.openxc.util.FileOpener;

/**
 * Record raw vehicle messages to a file as JSON.
 *
 * This data sink is a simple passthrough that records every raw vehicle
 * message as it arrives to a file on the device. It uses a heuristic to
 * detect different "trips" in the vehicle, and splits the recorded trace by
 * trip.
 *
 * The heuristic is very simple: if we haven't received any new data in a while,
 * consider the previous trip to have ended. When activity resumes, start a new
 * trip.
 */
public class FileRecorderSink implements VehicleDataSink {
    private final static String TAG = "FileRecorderSink";
    private final static int INTER_TRIP_THRESHOLD_MINUTES = 5;
    private static SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private FileOpener mFileOpener;
    private BufferedWriter mWriter;
    private Calendar mLastMessageReceived;

    public FileRecorderSink(FileOpener fileOpener) {
        mFileOpener = fileOpener;
    }

    @Override
    public synchronized void receive(VehicleMessage message)
            throws DataSinkException {
        if(mLastMessageReceived == null ||
                    Calendar.getInstance().getTimeInMillis()
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

        mLastMessageReceived = Calendar.getInstance();
        try {
            //JsonFormatter returns the message as a string in the JSON format
            String origMessage = JsonFormatter.serialize(message);
            //Checking to see if the formatted message string contains the mKey. If not, no need to clean it
            if(origMessage.contains("mKey"))
            {
                /*Sometimes the messages come with an mKey object attached, which is used as a unique
                identifier in the app. While this is useful for the app, it makes it harder to interpret
                the data when using a tracefile. This fix aims to clean up the message before it is
                written out. (See github.com/openxc/openxc-android/issues/253)
                */
                //Find the position of mKey and timestamp (timestamp always occurs after the mKey)
                int pos = origMessage.indexOf("mKey");
                int pos2 = origMessage.indexOf("timestamp");
                //Create a new string (still in the JSON format) that excludes all of the mKey
                String cleanedMessage = origMessage.substring(0, pos-1) + origMessage.substring(pos2-1);
                mWriter.write(cleanedMessage);
            } else
            {
                mWriter.write(origMessage);
            }
            mWriter.newLine();
        } catch(IOException e) {
            throw new DataSinkException("Unable to write message to file");
        }
    }

    @Override
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

    private synchronized void openTimestampedFile() throws IOException {
        Calendar calendar = Calendar.getInstance();
        String filename = sDateFormatter.format(
                calendar.getTime()) + ".json";
        if(mWriter != null) {
            close();
        }
        mWriter = mFileOpener.openForWriting(filename);
        Log.i(TAG, "Opened trace file " + filename + " for writing");
    }
}
