package com.openxc.sources;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openxc.messages.VehicleMessage;

/**
 * A common parent for all vehicle data sources.
 *
 * This class encapsulates funcationaliy common to most data sources. It accepts
 * and stores a SourceCallback reference (required by the
 * {@link com.openxc.sources.VehicleDataSource} interface) and implements a
 * {@link #handleMessage(VehicleMessage)} method for subclass to call
 * with each new message, regardless of its origin.
 */
public class BaseVehicleDataSource implements VehicleDataSource {
    private final static String TAG = "BaseVehicleDataSource";
    private SourceCallback mCallback;
    private final Lock mCallbackLock = new ReentrantLock();
    private final Condition mCallbackChanged = mCallbackLock.newCondition();

    public BaseVehicleDataSource() { }

    /**
     * Construct a new instance and set the callback.
     *
     * @param callback An object implementing the
     *      SourceCallback interface that should receive data from this
     *      source.
     */
    public BaseVehicleDataSource(SourceCallback callback) {
        setCallback(callback);
    }

    /**
     * Set the current source callback to the given value.
     *
     * @param callback a valid callback or null if you wish to stop the source
     *      from sending updates.
     */
    public void setCallback(SourceCallback callback) {
        try {
            mCallbackLock.lock();
            mCallback = callback;
            mCallbackChanged.signal();
        } finally {
            mCallbackLock.unlock();
        }
    }

    protected void disconnected() {
        if(mCallback != null) {
            mCallback.sourceDisconnected(this);
        }
    }

    protected void connected() {
        if(mCallback != null) {
            mCallback.sourceConnected(this);
        }
    }

    public boolean isConnected() {
        // TODO this is kind of weird, and probably says that this API needs
        // refactoring - we don't want to keep the Pipeline awake because it
        // thinks the sources like the RemoteListenerSource and
        // ApplicationSource (which are always in the pipeline) are "connected"
        // to a vehicle. maybe this isConnected method should only apply to the
        // VehicleInterface type of sources, but we don't currently track those
        // separately.
        return false;
    }

    /**
     * Clear the callback so no further updates are sent.
     *
     * Subclasses should be sure to call super.stop() so they also stop sending
     * updates when killed by a user.
     */
    public void stop() {
        disconnected();
        setCallback(null);
    }

    /**
     * Pass a new message to the callback, if set.
     *
     * @param message the new message object.
     */
    protected void handleMessage(VehicleMessage message) {
        message.timestamp();
        if(mCallback != null && message != null) {
            mCallback.receive(message);
        }
    }

    protected void waitForCallback() {
        try {
            mCallbackLock.lock();
            if(mCallback == null) {
                mCallbackChanged.await();
            }
        } catch(InterruptedException e) {
        } finally {
            mCallbackLock.unlock();
        }
    }


    /**
     * Return a string suitable as a tag for logging.
     */
    protected String getTag() {
      return TAG;
    }
}
