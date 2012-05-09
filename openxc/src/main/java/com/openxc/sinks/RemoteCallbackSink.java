package com.openxc.sinks;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import com.openxc.remote.RawMeasurement;

import com.openxc.sinks.BaseVehicleDataSink;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import android.util.Log;

public class RemoteCallbackSink extends BaseVehicleDataSink {
    private final static String TAG = "RemoteCallbackSink";

    private NotificationThread mNotificationThread;
    private BlockingQueue<String> mNotificationQueue;
    private int mListenerCount;
    private RemoteCallbackList<RemoteVehicleServiceListenerInterface>
            mListeners;

    public RemoteCallbackSink(Map<String, RawMeasurement> measurements) {
        super(measurements);
        init();
    }

    public RemoteCallbackSink() {
        init();
    }

    private void init() {
        mNotificationQueue = new LinkedBlockingQueue<String>();
        mListeners = new RemoteCallbackList<
            RemoteVehicleServiceListenerInterface>();
        mNotificationThread = new NotificationThread();
        mNotificationThread.start();
    }

    public synchronized void done() {
        if(mNotificationThread != null) {
            mNotificationThread.done();
        }
    }

    public synchronized void register(
            RemoteVehicleServiceListenerInterface listener) {
        mListeners.register(listener);
        ++mListenerCount;

        // send the last known value of all measurements to the new listener
        for(Map.Entry<String, RawMeasurement> entry :
                mMeasurements.entrySet()) {
            try {
                listener.receive(entry.getKey(), entry.getValue());
            } catch(RemoteException e) {
                Log.w(TAG, "Couldn't notify application " +
                        "listener -- did it crash?", e);
                break;
            }
        }
    }

    public void unregister(RemoteVehicleServiceListenerInterface listener) {
        mListeners.unregister(listener);
        --mListenerCount;
    }

    public int getListenerCount() {
        return mListenerCount;
    }

    public void receive(String measurementId, RawMeasurement measurement) {
        if(mNotificationQueue != null) {
            try  {
                mNotificationQueue.put(measurementId);
            } catch(InterruptedException e) {}
        }
    }

    private static void propagateMeasurement(
            RemoteCallbackList<RemoteVehicleServiceListenerInterface> callbacks,
            String measurementId,
            RawMeasurement measurement) {
        int i = callbacks.beginBroadcast();
        while(i > 0) {
            i--;
            try {
                callbacks.getBroadcastItem(i).receive(measurementId,
                        measurement);
            } catch(RemoteException e) {
                Log.w(TAG, "Couldn't notify application " +
                        "listener -- did it crash?", e);
            }
        }
        callbacks.finishBroadcast();
    }

    private class NotificationThread extends Thread {
        private boolean mRunning = true;

        private synchronized boolean isRunning() {
            return mRunning;
        }

        public synchronized void done() {
            Log.d(TAG, "Stopping notification thread");
            mRunning = false;
            // A context switch right can cause a race condition if we
            // used take() instead of poll(): when mRunning is set to
            // false and interrupt is called but we haven't called
            // take() yet, so nobody is waiting. By using poll we can not be
            // locked for more than 1s.
            interrupt();
        }

        public void run() {
            while(isRunning()) {
                String measurementId = null;
                try {
                    measurementId = mNotificationQueue.poll(1,
                            TimeUnit.SECONDS);
                } catch(InterruptedException e) {
                    Log.d(TAG, "Interrupted while waiting for a new " +
                            "item for notification -- likely shutting down");
                    return;
                }

                if(measurementId == null) {
                    continue;
                }

                RawMeasurement rawMeasurement = get(measurementId);
                if(rawMeasurement != null) {
                    propagateMeasurement(mListeners, measurementId,
                            rawMeasurement);
                }
            }
            Log.d(TAG, "Stopped measurement notifier");
        }
    }
};
