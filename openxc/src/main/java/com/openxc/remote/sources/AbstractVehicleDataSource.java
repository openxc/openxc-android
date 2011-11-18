package com.openxc.remote.sources;

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
            if(value instanceof Double) {
                mCallback.receive(name, (Double) value);
            } else if(value instanceof Integer) {
                mCallback.receive(name, new Double((Integer) value));
            } else if(value instanceof Boolean) {
                mCallback.receive(name, (Boolean) value);
            } else {
                Log.w(TAG, "Received data of an unsupported type from the " +
                        "data source: " + value + " a " + value.getClass());
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
