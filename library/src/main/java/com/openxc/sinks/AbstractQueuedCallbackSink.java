package com.openxc.sinks;

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
public abstract class AbstractQueuedCallbackSink implements VehicleDataSink {
    private final static String TAG = "AbstractQueuedCallbackSink";

    private NotificationThread mNotificationThread = new NotificationThread();
    private Lock mNotificationsLock = new ReentrantLock();
    private Condition mNotificationsChanged = mNotificationsLock.newCondition();
    private CopyOnWriteArrayList<VehicleMessage> mNotifications =
            new CopyOnWriteArrayList<>();

    public AbstractQueuedCallbackSink() {
        mNotificationThread.start();
    }

    @Override
    public synchronized void stop() {
        mNotificationThread.done();
    }

    @Override
    public void receive(VehicleMessage message) throws DataSinkException {
        try {
            mNotificationsLock.lock();
            mNotifications.add(message);
            mNotificationsChanged.signal();
        } finally {
            mNotificationsLock.unlock();
        }
    }

    /* Block until the notifications queue is cleared.
     */
    public void clearQueue() {
        try {
            mNotificationsLock.lock();
            while(!mNotifications.isEmpty()) {
                mNotificationsChanged.await();
            }
        } catch(InterruptedException e) {
        } finally {
            mNotificationsLock.unlock();
        }
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

        @Override
        public void run() {
            Log.d(TAG, "Starting notification thread");
            while(isRunning()) {
                mNotificationsLock.lock();
                try {
                    if(mNotifications.isEmpty()) {
                        mNotificationsChanged.await();
                    }

                    Iterator<VehicleMessage> it = mNotifications.iterator();
                    CopyOnWriteArrayList<VehicleMessage> deleted =
                            new CopyOnWriteArrayList<>(mNotifications);
                    while(it.hasNext()) {
                        VehicleMessage message = it.next();
                        propagateMessage(message);
                        deleted.add(message);
                    }
                    mNotifications.removeAll(deleted) ;

                } catch(InterruptedException e) {
                    Log.d(TAG, "Interrupted while waiting for a new " +
                            "item for notification -- likely shutting down");
                    break;
                } finally {
                    mNotificationsChanged.signal();
                    mNotificationsLock.unlock();
                }
            }
            Log.d(TAG, "Stopped notification thread");
        }
    }
}
