package com.openxc.remote.sinks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.remote.RawMeasurement;

import android.util.Log;

public class FileRecorderSink implements VehicleDataSinkInterface {
    private final static String TAG = "FileRecorderSink";
    private final static String DEFAULT_OUTPUT_FILE =
            "/sdcard/openxc/trace.json";

    private BufferedWriter mWriter;

    public FileRecorderSink() {
        try {
            File file = new File(DEFAULT_OUTPUT_FILE);
            file.getParentFile().mkdirs();
            mWriter = new BufferedWriter(new FileWriter(file));
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + DEFAULT_OUTPUT_FILE +
                    " for writing", e);
            mWriter = null;
        }
    }

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

        try {
            mWriter.write(object.toString());
            mWriter.newLine();
        } catch(IOException e) {
            Log.w(TAG, "Unable to write measurement to file", e);
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
}
