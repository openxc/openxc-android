package com.openxc.sinks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    private Lock mNotificationsLock;
    private Condition mNotificationReceived;
    private Set<String> mNotifications;

    public AbstractQueuedCallbackSink() {
        mNotifications = new HashSet<String>(32);
        mNotificationsLock = new ReentrantLock();
        mNotificationReceived = mNotificationsLock.newCondition();
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
        mNotificationsLock.lock();
        // TODO what we if we added the actual measurement here, that would save
        // a lookup later
        mNotifications.add(rawMeasurement.getName());
        mNotificationReceived.signal();
        mNotificationsLock.unlock();
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
                mNotificationsLock.lock();
                if(mNotifications.isEmpty()) {
                    try {
                        mNotificationReceived.await();
                    } catch(InterruptedException e) {
                        Log.d(TAG, "Interrupted while waiting for a new " +
                                "item for notification -- likely shutting down");
                        mNotificationsLock.unlock();
                        return;
                    }
                }

                Iterator<String> it = mNotifications.iterator();
                if(it.hasNext()) {
                    String measurementId = it.next();
                    it.remove();
                    RawMeasurement rawMeasurement = get(measurementId);
                    if(rawMeasurement != null) {
                        propagateMeasurement(measurementId, rawMeasurement);
                    }
                }
                mNotificationsLock.unlock();
            }
            Log.d(TAG, "Stopped measurement notifier");
        }
    }
}
