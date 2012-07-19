package com.openxc.sources;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openxc.remote.RawMeasurement;

import com.openxc.sources.SourceCallback;

/**
 * A common parent for all vehicle data sources.
 *
 * This class encapsulates funcationaliy common to most data sources. It accepts
 * and stores a SourceCallback reference (required by the
 * {@link com.openxc.sources.VehicleDataSource} interface) and implements a
 * {@link #handleMessage(RawMeasurement)} method for subclass to call
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
     * @param measurement the new measurement object.
     */
    protected void handleMessage(RawMeasurement measurement) {
        if(mCallback != null && measurement != null) {
            mCallback.receive(measurement);
        }
    }

    protected void handleMessage(String serializedMeasurement) {
        handleMessage(RawMeasurement.deserialize(serializedMeasurement));
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
