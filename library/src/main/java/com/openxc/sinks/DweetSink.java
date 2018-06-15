package com.openxc.sinks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.buglabs.dweetlib.DweetLib;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.formatters.JsonFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Sends bundles of all incoming vehicle data to Dweet.io.
 *
 *
 */
public class DweetSink extends ContextualVehicleDataSink {
    private final static String TAG = "DweetSink";
    private final static int UPLOAD_BATCH_SIZE = 25;
    private final static int MAXIMUM_QUEUED_RECORDS = 1000;
    private final static int HTTP_TIMEOUT = 5000;

    private String mThingName;
    private Context mContext;
    private BlockingQueue<VehicleMessage> mRecordQueue =
            new LinkedBlockingQueue<>(MAXIMUM_QUEUED_RECORDS);
    private Lock mQueueLock = new ReentrantLock();
    private Condition mRecordsQueued = mQueueLock.newCondition();
    private DweetThread mDweeter = new DweetThread();

    /**
     * Initialize and start a new DweetSink immediately.
     *
     * @param thing_name the Thing Name to send Dweets to with the JSON data.
     */
    public DweetSink(Context context, String thing_name) {
        super(context);
        mThingName = thing_name;
        mContext = context;
    }

    @Override
    public void stop() {
        mDweeter.done();
    }

    @Override
    public void receive(VehicleMessage message) {
        mRecordQueue.offer(message);
        if(mRecordQueue.size() >= UPLOAD_BATCH_SIZE) {
            try {
                mQueueLock.lock();
                mRecordsQueued.signal();
            } finally {
                mQueueLock.unlock();
            }
        }
    }

    private static class UploaderException extends DataSinkException {
        private static final long serialVersionUID = 7436279598279767619L;

        public UploaderException() { }

        public UploaderException(String message) {
            super(message);
        }
    }

    private class DweetThread extends Thread {
        private boolean mRunning = true;
        ArrayList<VehicleMessage> records ;
        private Handler dweetHandler;

        public DweetThread() {
            start();
            dweetHandler = new Handler(Looper.getMainLooper());
            dweetHandler.postDelayed(runnable, 1000);
        }

        @Override
        public void run() {
            while(mRunning) {
                try {
                    records = getRecords();
                } catch(InterruptedException e) {
                    Log.w(TAG, "Dweeter was interrupted", e);
                    break;
                }
            }
        }

        public void done() {
            mRunning = false;
        }

        private ArrayList<VehicleMessage> getRecords() throws InterruptedException {
            try {
                mQueueLock.lock();
                while(mRecordQueue.isEmpty()) {
                    // the queue is already thread safe, but we use this lock to get
                    // a condition variable we can use to signal when a batch has
                    // been queued.
                    mRecordsQueued.await(5, TimeUnit.SECONDS);
                }

                ArrayList<VehicleMessage> records = new ArrayList<>();
                mRecordQueue.drainTo(records, UPLOAD_BATCH_SIZE);
                return records;
            } finally {
                mQueueLock.unlock();
            }
        }
        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(mRunning) {
                    DweetLib.DweetCallback cb = new DweetLib.DweetCallback() {
                        @Override
                        public void callback(ArrayList<Object> ar) {
                            Integer result = (Integer) ar.get(0);
                        }
                    };

                    JSONObject jsonObj = null;
                    try {
                        if(records!=null) {
                            // add all connected sensor data to JSON object
                            jsonObj = new JSONObject();
                            JSONArray array = new JSONArray(JsonFormatter.serialize(records));
                            for (int i = 0; i < array.length(); i++) {
                                jsonObj.put((String) array.getJSONObject(i).get("name"), array.getJSONObject(i).get("value"));
                            }
                            String str = DweetLib.getInstance(mContext).sendDweet(jsonObj, mThingName, "", this, cb, true);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,"cfg dweet error" + e);
                    }
                    // restart the dweet timer
                    dweetHandler.postDelayed(this, 1000);
                }
            }

        };
    }
}
