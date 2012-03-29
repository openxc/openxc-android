package com.openxc.remote.sinks;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.util.Date;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.openxc.remote.RawMeasurement;

import android.content.Context;
import android.util.Log;

public class UploaderSink implements VehicleDataSinkInterface {
    private final static String TAG = "UploaderSink";
    private final static int UPLOAD_BATCH_SIZE = 100;

    private BlockingQueue<String> mRecordQueue;

    public UploaderSink(Context context) {
        mRecordQueue = new LinkedBlockingQueue<String>();
    }

    public void stop() { }

    public void receive(String measurementId, Object value, Object event) {
        double timestamp = System.currentTimeMillis() / 1000.0;

        JSONObject object = new JSONObject();
        try {
            object.put("name", measurementId);
            object.put("timestamp", timestamp);
            object.put("value", value);
            if(event != null) {
                object.put("event", event);
            }
        } catch(JSONException e) {
            Log.w(TAG, "Unable to create JSON for trace file", e);
            return;
        }
        mRecordQueue.add(object.toString());
        if(mRecordQueue.size() >= UPLOAD_BATCH_SIZE) {
            (new UploaderThread()).start();
        }
    }

    private class UploaderThread extends Thread {
        private boolean mRunning = true;

        public void run() {
            ArrayList<String> records = new ArrayList();
            mRecordQueue.drainTo(records, UPLOAD_BATCH_SIZE);
            Log.d(TAG, "Would have uploaded " + records.size() + " items");
        }
    }
}
