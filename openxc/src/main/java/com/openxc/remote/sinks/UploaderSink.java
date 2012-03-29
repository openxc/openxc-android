package com.openxc.remote.sinks;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;

import org.apache.http.impl.client.DefaultHttpClient;

import com.openxc.remote.RawMeasurement;

import android.content.Context;
import android.util.Log;

public class UploaderSink implements VehicleDataSinkInterface {
    private final static String TAG = "UploaderSink";
    private final static String UPLOAD_URL =
            "http://fiesta.eecs.umich.edu:5000/records";
    private final static int UPLOAD_BATCH_SIZE = 200;
    private final static int MAXIMUM_QUEUED_RECORDS = 2000;
    private static DecimalFormat sTimestampFormatter =
            new DecimalFormat("##########.000000");

    private BlockingQueue<JSONObject> mRecordQueue;
    private HttpClient mClient;

    public UploaderSink(Context context) {
        mRecordQueue = new LinkedBlockingQueue<JSONObject>(
                MAXIMUM_QUEUED_RECORDS);
        mClient = new DefaultHttpClient();
    }

    public void stop() { }

    public void receive(String measurementId, Object value, Object event) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        String timestampString = sTimestampFormatter.format(timestamp);

        JSONObject object = new JSONObject();
        try {
            object.put("name", measurementId);
            object.put("timestamp", timestampString);
            object.put("value", value);
            if(event != null) {
                object.put("event", event);
            }
        } catch(JSONException e) {
            Log.w(TAG, "Unable to create JSON for trace file", e);
            return;
        }
        mRecordQueue.offer(object);
        if(mRecordQueue.size() >= UPLOAD_BATCH_SIZE) {
            (new UploaderThread()).start();
        }
    }

    private class UploaderThread extends Thread {
        private boolean mRunning = true;

        public void run() {
            ArrayList<JSONObject> records = new ArrayList();
            mRecordQueue.drainTo(records, UPLOAD_BATCH_SIZE);

            JSONArray recordArray = new JSONArray();
            for(JSONObject record : records) {
                recordArray.put(record);
            }

            JSONObject data = new JSONObject();
            try {
                data.put("records", recordArray);
            } catch(JSONException e) {
                Log.w(TAG, "Unable to create JSON for uploading records", e);
                return;
            }

            final HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(UPLOAD_URL);
            try {
                ByteArrayEntity entity = new ByteArrayEntity(
                        data.toString().getBytes("UTF8"));
                entity.setContentEncoding(
                        new BasicHeader("Content-Type", "application/json"));
                request.setEntity(entity);
            } catch(UnsupportedEncodingException e) {
                Log.w(TAG, "Couldn't encode records for uploading", e);
                return;
            }

            try {
                HttpResponse response = client.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == HttpStatus.SC_CREATED) {
                    Log.d(TAG, "Uploaded " + records.size() + " records");
                } else {
                    Log.w(TAG, "Got unxpected status code: " + statusCode);
                }
            } catch(IOException e) {
                Log.w(TAG, "Problem uploading the record", e);
            }
        }
    }
}
