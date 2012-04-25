package com.openxc.remote.sinks;

import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Date;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.remote.RawMeasurement;

import com.openxc.util.FileOpener;

import android.util.Log;

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

    public FileRecorderSink(FileOpener fileOpener) {
        mFileOpener = fileOpener;
        openTimestampedFile();
    }

    public void receive(String measurementId, RawMeasurement measurement) {
    }

    /**
     * Record a message to a file, selected by the current time.
     */
    public void receive(String measurementId, Object value, Object event) {
        JSONObject object = new JSONObject();
        try {
            object.put("name", measurementId);
            object.put("value", value);
            if(event != null) {
                object.put("event", event);
            }
        } catch(JSONException e) {
            Log.w(TAG, "Unable to create JSON for trace file", e);
            return;
        }

        double timestamp = System.currentTimeMillis() / 1000.0;
        String timestampString = sTimestampFormatter.format(timestamp);
        if(mWriter != null) {
            if((new Date()).getHours() != mLastFileCreated.getHours()) {
                // flip to a new file every hour
                openTimestampedFile();
            }

            try {
                mWriter.write(timestampString + ": " + object.toString());
                mWriter.newLine();
            } catch(IOException e) {
                Log.w(TAG, "Unable to write measurement to file", e);
            }
        } else {
            Log.w(TAG, "No valid writer - not recording trace line");
        }

    }

    public void stop() {
        if(mWriter != null) {
            try {
                mWriter.close();
            } catch(IOException e) {
                Log.w(TAG, "Unable to close output file", e);
            }
            mWriter = null;
        }
    }

    private void openTimestampedFile() {
        mLastFileCreated = new Date();
        String filename = sDateFormatter.format(mLastFileCreated) + ".json";
        Log.i(TAG, "Opening trace file " + filename + " for writing");
        try {
            mWriter = mFileOpener.openForWriting(filename);
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + filename + " for writing", e);
            mWriter = null;
        }
    }
}
