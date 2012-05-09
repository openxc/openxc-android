package com.openxc.sources;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openxc.sources.SourceCallback;

/**
 * The BaseVehicleDataSource contains functions common to all vehicle data
 * sources.
 *
 * TODO
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

    public void setCallback(SourceCallback callback) {
        mCallbackLock.lock();
        mCallback = callback;
        mCallbackChanged.signal();
        mCallbackLock.unlock();
    }

    public void stop() {
        setCallback(null);
    }


    protected void handleMessage(String name, Object value) {
        handleMessage(name, value, null);
    }

    protected void handleMessage(String name, Object value, Object event) {
        if(mCallback != null) {
            mCallback.receive(name, value, event);
        }
    }

    protected SourceCallback getCallback() {
        return mCallback;
    }

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
