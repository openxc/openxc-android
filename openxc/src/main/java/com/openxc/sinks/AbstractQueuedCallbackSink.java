package com.openxc.sinks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.openxc.remote.RawMeasurement;

import android.util.Log;

/**
 * Functionality to notify multiple clients asynchronously of new measurements.
 *
 * This class encapsulates the functionality to keep a thread-safe list of
 * listeners that want to be notified of updates asyncronously. Subclasses need
 * only to implement the {@link #propagateMeasurement(String, RawMeasurement)}
 * to add the actual logic for looping over the list of receivers and send them
 * new values.
 *
 * New measurments are queued up and propagated to receivers in a separate
 * thread, to avoid blocking the original sender of the data.
 */
public abstract class AbstractQueuedCallbackSink extends BaseVehicleDataSink {
    private final static String TAG = "AbstractQueuedCallbackSink";
    private NotificationThread mNotificationThread;
    private BlockingQueue<String> mNotificationQueue;

    public AbstractQueuedCallbackSink() {
        mNotificationQueue = new LinkedBlockingQueue<String>();
        mNotificationThread = new NotificationThread();
        mNotificationThread.start();
    }

    abstract protected void propagateMeasurement(String measurementId,
            RawMeasurement measurement);

    public synchronized void stop() {
        if(mNotificationThread != null) {
            mNotificationThread.done();
        }
    }

    public void receive(RawMeasurement rawMeasurement)
            throws DataSinkException {
        super.receive(rawMeasurement);
        try  {
            mNotificationQueue.put(rawMeasurement.getName());
        } catch(InterruptedException e) {}
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

                if(measurementId != null) {
                    RawMeasurement rawMeasurement = get(measurementId);
                    if(rawMeasurement != null) {
                        propagateMeasurement(measurementId, rawMeasurement);
                    }
                }
            }
            Log.d(TAG, "Stopped measurement notifier");
        }
    }
}
