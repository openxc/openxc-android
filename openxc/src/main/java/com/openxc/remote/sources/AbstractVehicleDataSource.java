package com.openxc.remote.sources;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;

import android.util.Log;

public abstract class AbstractVehicleDataSource
        implements VehicleDataSourceInterface {

    private static final String TAG = "AbstractVehicleDataSource";

    private VehicleDataSourceCallbackInterface mCallback;
    private Context mContext;

    public AbstractVehicleDataSource() { }

    public AbstractVehicleDataSource(Context context,
            VehicleDataSourceCallbackInterface callback) {
        mContext = context;
        setCallback(callback);
    }

    public AbstractVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        this(null, callback);
    }

    public void setCallback(VehicleDataSourceCallbackInterface callback) {
        mCallback = callback;
    }

    protected void handleMessage(String name, Object value) {
        if(mCallback != null) {
            Method method;
            try {
                method = VehicleDataSourceCallbackInterface.class.getMethod(
                        "receive", String.class, value.getClass());
            } catch(NoSuchMethodException e) {
                Log.w(TAG, "Received data of an unsupported type from the " +
                        "data source: " + value + ", a " + value.getClass());
                return;
            }

            try {
                // TODO does these cast value properly to the right type?
                method.invoke(mCallback, name, value);
            } catch(IllegalAccessException e) {
                Log.w(TAG, "Data receiver method is private", e);
            } catch(InvocationTargetException e) {
                Log.w(TAG, "Unable to call data receive method", e);
            }
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected VehicleDataSourceCallbackInterface getCallback() {
        return mCallback;
    }
}
