package com.openxc.remote.sinks;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.remote.RawMeasurement;

import android.util.Log;
import android.content.Context;

public class FileRecorderSink implements VehicleDataSinkInterface {
    private final static String TAG = "FileRecorderSink";
    private final static String DEFAULT_FILENAME = "trace.json";
    private static DecimalFormat sTimestampFormatter =
            new DecimalFormat("##########.000000");

    private BufferedWriter mWriter;

    public FileRecorderSink(Context context) {
        try {
            OutputStream outputStream = context.openFileOutput(
                    DEFAULT_FILENAME,
                    Context.MODE_WORLD_READABLE | Context.MODE_APPEND);
            mWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        } catch(IOException e) {
            Log.w(TAG, "Unable to open " + DEFAULT_FILENAME +
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

        double timestamp = System.currentTimeMillis() / 1000.0;
        String timestampString = sTimestampFormatter.format(timestamp);
        if(mWriter != null) {
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
}
