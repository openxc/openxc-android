package com.openxc.sources;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openxc.sources.SourceCallback;

/**
 * A common parent for all vehicle data sources.
 *
 * This class encapsulates funcationaliy common to most data sources. It accepts
 * and stores a SourceCallback reference (required by the
 * {@link com.opnexc.sources.VehicleDataSource} interface) and implements a
 * {@link #handleMessage(String, Object, Object)} method for subclass to call
 * with each new measurement, regardless of its origin.
 */
public class BaseVehicleDataSource implements VehicleDataSource {
    private SourceCallback mCallback;
    private final Lock mCallbackLock;
    private final Condition mCallbackChanged;

    public BaseVehicleDataSource() {
        mCallbackLock = new ReentrantLock();
        mCallbackChanged = mCallbackLock.newCondition();
    }

    /**
     * Construct a new instance and set the callback.
     *
     * @param callback An object implementing the
     *      SourceCallback interface that should receive data from this
     *      source.
     */
    public BaseVehicleDataSource(SourceCallback callback) {
        this();
        setCallback(callback);
    }

    /**
     * Set the current source callback to the given value.
     *
     * @param callback a valid callback or null if you wish to stop the source
     *      from sending updates.
     */
    public void setCallback(SourceCallback callback) {
        mCallbackLock.lock();
        mCallback = callback;
        mCallbackChanged.signal();
        mCallbackLock.unlock();
    }

    /**
     * Clear the callback so no further updates are sent.
     *
     * Subclasses should be sure to call super.stop() so they also stop sending
     * updates when killed by a user.
     */
    public void stop() {
        setCallback(null);
    }

    /**
     * Pass a new measurement to the callback, if set.
     *
     * @param name the name of the new measurement
     * @param value the value of the new measurement
     * @param event an optional event associated with the measurement
     */
    protected void handleMessage(String name, Object value, Object event) {
        if(mCallback != null) {
            mCallback.receive(name, value, event);
        }
    }

    protected void handleMessage(String name, Object value) {
        handleMessage(name, value, null);
    }

    protected SourceCallback getCallback() {
        return mCallback;
    }

    /**
     * Block the current thread until the callback is not null.
     *
     * This is useful if the data source's collection process is expensive and
     * should not be started until absolutely necessary.
     */
    protected void waitForCallbackInitialization() {
        mCallbackLock.lock();
        while(getCallback() == null) {
            try {
                mCallbackChanged.await();
            } catch(InterruptedException e) { }
        }
        mCallbackLock.unlock();
    }
}
