package com.openxc.remote.sources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.util.Log;

import com.openxc.remote.sources.SourceCallback;

/**
 * The BaseVehicleDataSource contains functions common to all vehicle data
 * sources.
 */
public class BaseVehicleDataSource implements VehicleDataSource {

    private static final String TAG = "BaseVehicleDataSource";
    private static final String RECEIVE_METHOD_NAME = "receive";

    private SourceCallback mCallback;

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

    public void setCallback(SourceCallback callback) {
        mCallback = callback;
    }

    public void stop() {
        // do nothing by default
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
}
