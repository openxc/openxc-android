package com.openxc.sinks;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.openxc.remote.RawMeasurement;

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

    private NotificationThread mNotificationThread = new NotificationThread();
    private Lock mNotificationsLock = new ReentrantLock();
    private Condition mNotificationReceived = mNotificationsLock.newCondition();
    private ConcurrentHashMap<String, RawMeasurement> mNotifications =
            new ConcurrentHashMap<String, RawMeasurement>(32);

    public AbstractQueuedCallbackSink() {
        mNotificationThread.start();
    }

    public synchronized void stop() {
        mNotificationThread.done();
    }

    public boolean receive(RawMeasurement rawMeasurement)
            throws DataSinkException {
        super.receive(rawMeasurement);
        mNotificationsLock.lock();
        mNotifications.put(rawMeasurement.getName(), rawMeasurement);
        mNotificationReceived.signal();
        mNotificationsLock.unlock();
        return true;
    }

    abstract protected void propagateMeasurement(String measurementId,
            RawMeasurement measurement);

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
                try {
                    if(mNotifications.isEmpty()) {
                        mNotificationReceived.await();
                    }
                } catch(InterruptedException e) {
                    Log.d(TAG, "Interrupted while waiting for a new " +
                            "item for notification -- likely shutting down");
                    return;
                } finally {
                    mNotificationsLock.unlock();
                }

                // This iterator is weakly consistent, so we don't need the lock
                Iterator<RawMeasurement> it = mNotifications.values().iterator();
                while(it.hasNext()) {
                    RawMeasurement measurement = it.next();
                    propagateMeasurement(measurement.getName(), measurement);
                    it.remove();
                }
            }
            Log.d(TAG, "Stopped measurement notifier");
        }
    }
}
