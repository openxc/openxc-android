package com.openxc.sinks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URI;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;

import org.apache.http.impl.client.DefaultHttpClient;

import com.openxc.measurements.serializers.JsonSerializer;

import com.openxc.remote.RawMeasurement;

import android.content.Context;
import android.util.Log;

public class UploaderSink extends ContextualVehicleDataSink {
    private final static String TAG = "UploaderSink";
    private final static int UPLOAD_BATCH_SIZE = 25;
    private final static int MAXIMUM_QUEUED_RECORDS = 2000;
    private final static int HTTP_TIMEOUT = 5000;
    private static DecimalFormat sTimestampFormatter =
            new DecimalFormat("##########.000000");

    private URI mUri;
    private BlockingQueue<JSONObject> mRecordQueue;
    private Lock mQueueLock;
    private Condition mRecordsQueuedSignal;
    private UploaderThread mUploader;

    public UploaderSink(Context context, URI uri) {
        super(context);
        mUri = uri;
        mRecordQueue = new LinkedBlockingQueue<JSONObject>(
                MAXIMUM_QUEUED_RECORDS);
        mQueueLock = new ReentrantLock();
        mRecordsQueuedSignal = mQueueLock.newCondition();
        mUploader = new UploaderThread();
        mUploader.start();
    }

    public UploaderSink(Context context, String path)
            throws java.net.URISyntaxException {
        this(context, new URI(path));
    }

    public void stop() {
        super.stop();
        mUploader.done();
    }

    public void receive(RawMeasurement measurement) {
        double timestamp = System.currentTimeMillis() / 1000.0;
        String timestampString = sTimestampFormatter.format(timestamp);

        JSONObject object = JsonSerializer.preSerialize(measurement.getName(),
                measurement.getValue(), measurement.getEvent());
        try {
            object.put("timestamp", timestampString);
        } catch(JSONException e) {
            Log.w(TAG, "Unable to create JSON for trace file", e);
            return;
        }
        mRecordQueue.offer(object);
        if(mRecordQueue.size() >= UPLOAD_BATCH_SIZE) {
            mQueueLock.lock();
            mRecordsQueuedSignal.signal();
            mQueueLock.unlock();
        }
    }

    public static boolean validatePath(String path) {
        if(path == null) {
            Log.w(TAG, "Uploading path not set (it's " + path + ")");
            return false;
        }

        try {
            URI uri = new URI(path);
            return uri.isAbsolute();
        } catch(java.net.URISyntaxException e) {
            return false;
        }
    }

    private class UploaderException extends Exception { }

    private class UploaderThread extends Thread {
        private boolean mRunning = true;

        public void done() {
            mRunning = false;
        }

        private JSONObject constructRequestData(ArrayList<JSONObject> records)
                throws UploaderException {
            JSONArray recordArray = new JSONArray();
            for(JSONObject record : records) {
                recordArray.put(record);
            }

            JSONObject data = new JSONObject();
            try {
                data.put("records", recordArray);
            } catch(JSONException e) {
                Log.w(TAG, "Unable to create JSON for uploading records", e);
                throw new UploaderException();
            }
            return data;
        }

        private HttpPost constructRequest(JSONObject data)
                throws UploaderException {
            HttpPost request = new HttpPost(mUri);
            try {
                ByteArrayEntity entity = new ByteArrayEntity(
                        data.toString().getBytes("UTF8"));
                entity.setContentEncoding(
                        new BasicHeader("Content-Type", "application/json"));
                request.setEntity(entity);
            } catch(UnsupportedEncodingException e) {
                Log.w(TAG, "Couldn't encode records for uploading", e);
                throw new UploaderException();
            }
            return request;
        }

        private void makeRequest(HttpPost request) throws InterruptedException {
            HttpParams parameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(parameters, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(parameters, HTTP_TIMEOUT);
            final HttpClient client = new DefaultHttpClient(parameters);
            try {
                HttpResponse response = client.execute(request);
                final int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode != HttpStatus.SC_CREATED) {
                    Log.w(TAG, "Got unxpected status code: " + statusCode);
                }
            } catch(IOException e) {
                Log.w(TAG, "Problem uploading the record", e);
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e2) {
                    Log.w(TAG, "Uploader interrupted after an error", e2);
                    throw e2;
                }
            }
        }

        private ArrayList<JSONObject> waitForRecords()
                throws InterruptedException {
            mQueueLock.lock();
            mRecordsQueuedSignal.await();

            ArrayList<JSONObject> records = new ArrayList<JSONObject>();
            mRecordQueue.drainTo(records, UPLOAD_BATCH_SIZE);

            mQueueLock.unlock();
            return records;
        }

        public void run() {
            while(mRunning) {
                try {
                    ArrayList<JSONObject> records = waitForRecords();
                    JSONObject data = constructRequestData(records);
                    HttpPost request = constructRequest(data);
                    makeRequest(request);
                } catch(UploaderException e) {
                    Log.w(TAG, "Problem uploading the record", e);
                } catch(InterruptedException e) {
                    Log.w(TAG, "Uploader was interrupted", e);
                    break;
                }
            }
        }
    }
}
