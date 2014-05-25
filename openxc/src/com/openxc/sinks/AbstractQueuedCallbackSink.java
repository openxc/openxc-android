package com.openxc.sinks;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.VehicleMessage;

/**
 * Functionality to notify multiple clients asynchronously of new measurements.
 *
 * This class encapsulates the functionality to keep a thread-safe list of
 * listeners that want to be notified of updates asyncronously. Subclasses need
 * only to implement the {@link #propagateMessage(String, VehicleMessage)}
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
    private ConcurrentHashMap<String, NamedVehicleMessage> mNotifications =
            new ConcurrentHashMap<String, NamedVehicleMessage>(32);

    public AbstractQueuedCallbackSink() {
        mNotificationThread.start();
    }

    public synchronized void stop() {
        mNotificationThread.done();
    }

    // TODO how is this going to work for other messages? what key to do they
    // use to register?
    public boolean receive(NamedVehicleMessage message)
            throws DataSinkException {
        super.receive(message);
        mNotificationsLock.lock();
        mNotifications.put(message.getName(), message);
        mNotificationReceived.signal();
        mNotificationsLock.unlock();
        return true;
    }

    abstract protected void propagateMessage(String name,
            NamedVehicleMessage message);

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
                Iterator<NamedVehicleMessage> it = mNotifications.values().iterator();
                while(it.hasNext()) {
                    NamedVehicleMessage message = it.next();
                    propagateMessage(message.getName(), message);
                    it.remove();
                }
            }
            Log.d(TAG, "Stopped message notifier");
        }
    }
}
