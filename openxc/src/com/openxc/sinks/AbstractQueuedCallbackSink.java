package com.openxc.sinks;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.openxc.messages.VehicleMessage;

/**
 * Functionality to notify multiple clients asynchronously of new measurements.
 *
 * This class encapsulates the functionality to keep a thread-safe list of
 * listeners that want to be notified of updates asynchronously. Subclasses need
 * only to implement the {@link #propagateMessage(VehicleMessage)}
 * to add the actual logic for looping over the list of receivers and send them
 * new values.
 *
 * New measurements are queued up and propagated to receivers in a separate
 * thread, to avoid blocking the original sender of the data.
 */
public abstract class AbstractQueuedCallbackSink extends BaseVehicleDataSink {
    private final static String TAG = "AbstractQueuedCallbackSink";

    private NotificationThread mNotificationThread = new NotificationThread();
    private Lock mNotificationsLock = new ReentrantLock();
    private Condition mNotificationReceived = mNotificationsLock.newCondition();
    private CopyOnWriteArrayList<VehicleMessage> mNotifications =
            new CopyOnWriteArrayList<VehicleMessage>();

    public AbstractQueuedCallbackSink() {
        mNotificationThread.start();
    }

    public synchronized void stop() {
        mNotificationThread.done();
    }

    public boolean receive(VehicleMessage message)
            throws DataSinkException {
        super.receive(message);
        mNotificationsLock.lock();
        mNotifications.add(message);
        mNotificationReceived.signal();
        mNotificationsLock.unlock();
        return true;
    }

    abstract protected void propagateMessage(VehicleMessage message);

    private class NotificationThread extends Thread {
        private boolean mRunning = true;

        private synchronized boolean isRunning() {
            return mRunning;
        }

        public synchronized void done() {
            Log.d(TAG, "Stopping message notifier");
            mRunning = false;
            // A context switch right can cause a race condition if we
            // used take() instead of poll(): when mRunning is set to
            // false and interrupt is called but we haven't called
            // take() yet, so nobody is waiting. By using poll we can not be
            // locked for more than 1s.
            interrupt();
        }

        public void run() {
            Log.d(TAG, "Starting notification thread");
            while(isRunning()) {
                mNotificationsLock.lock();
                try {
                    if(mNotifications.isEmpty()) {
                        mNotificationReceived.await();
                    }

                    // This iterator is weakly consistent, so we don't need the lock
                    Iterator<VehicleMessage> it = mNotifications.iterator();
                    CopyOnWriteArrayList<VehicleMessage> deleted =
                            new CopyOnWriteArrayList<VehicleMessage>(mNotifications);
                    while(it.hasNext()) {
                        VehicleMessage message = it.next();
                        propagateMessage(message);
                        deleted.add(message);
                    }
                    mNotifications.removeAll(deleted);

                } catch(InterruptedException e) {
                    Log.d(TAG, "Interrupted while waiting for a new " +
                            "item for notification -- likely shutting down");
                    break;
                } finally {
                    mNotificationsLock.unlock();
                }

                // This iterator is weakly consistent, so we don't need the lock
                Iterator<VehicleMessage> it = mNotifications.iterator();
                CopyOnWriteArrayList<VehicleMessage> deleted =
                        new CopyOnWriteArrayList<VehicleMessage>(mNotifications);
                while(it.hasNext()) {
                    VehicleMessage message = it.next();
                    propagateMessage(message);
                    deleted.add(message);
                }
                mNotifications.removeAll(deleted);
            }
            Log.d(TAG, "Stopped notification thread");
        }
    }
}
