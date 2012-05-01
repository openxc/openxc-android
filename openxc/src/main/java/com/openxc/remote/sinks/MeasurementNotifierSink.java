package com.openxc.remote.sinks;

import java.util.Collections;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import com.openxc.remote.RawMeasurement;

import com.openxc.remote.sinks.BaseVehicleDataSink;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import android.util.Log;

public class MeasurementNotifierSink extends BaseVehicleDataSink {
    private final static String TAG = "MeasurementNotifierSink";

    private NotificationThread mNotificationThread;
    private BlockingQueue<String> mNotificationQueue;
    private Map<String, RemoteCallbackList<
        RemoteVehicleServiceListenerInterface>> mListeners;

    public MeasurementNotifierSink(Map<String, RawMeasurement> measurements) {
        super(measurements);
        init();
    }

    public MeasurementNotifierSink() {
        init();
    }

    private void init() {
        mNotificationQueue = new LinkedBlockingQueue<String>();
        mListeners = Collections.synchronizedMap(
                new HashMap<String, RemoteCallbackList<
                RemoteVehicleServiceListenerInterface>>());
        mNotificationThread = new NotificationThread();
        mNotificationThread.start();
    }

    public synchronized void done() {
        if(mNotificationThread != null) {
            mNotificationThread.done();
        }
    }

    public void register(String measurementId,
            RemoteVehicleServiceListenerInterface listener) {
        getOrCreateCallbackList(measurementId).register(listener);
    }

    public void unregister(String measurementId,
            RemoteVehicleServiceListenerInterface listener) {
        getOrCreateCallbackList(measurementId).unregister(listener);
    }

    public int getListenerCount() {
        return mListeners.size();
    }

    public void receive(String measurementId,
            RawMeasurement measurement) {
        if(mListeners.containsKey(measurementId)) {
            try  {
                mNotificationQueue.put(measurementId);
            } catch(InterruptedException e) {}
        }
    }

    private RemoteCallbackList<RemoteVehicleServiceListenerInterface>
            getOrCreateCallbackList(String measurementId) {
        RemoteCallbackList<RemoteVehicleServiceListenerInterface>
            callbackList = mListeners.get(measurementId);
        if(callbackList == null) {
            callbackList = new RemoteCallbackList<
                RemoteVehicleServiceListenerInterface>();
            mListeners.put(measurementId, callbackList);
        }
        return callbackList;
    }

    private void propagateMeasurement(
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

                RemoteCallbackList<RemoteVehicleServiceListenerInterface>
                    callbacks = mListeners.get(measurementId);
                if(mMeasurements != null) {
                    RawMeasurement rawMeasurement = mMeasurements.get(
                            measurementId);
                    propagateMeasurement(callbacks, measurementId,
                            rawMeasurement);
                }
            }
            Log.d(TAG, "Stopped USB listener");
        }
    }
};
