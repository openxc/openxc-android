package com.openxc.remote.sources;

import android.content.Context;

public abstract class AbstractVehicleDataSource
        implements VehicleDataSourceInterface {
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
            } else if(value instanceof Boolean) {
                mCallback.receive(name, (Boolean) value);
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
