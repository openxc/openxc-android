package com.openxc.remote.sources;

public abstract class AbstractVehicleDataSource
        implements VehicleDataSourceInterface {
    VehicleDataSourceCallbackInterface mCallback;

    public AbstractVehicleDataSource() { }

    public AbstractVehicleDataSource(
            VehicleDataSourceCallbackInterface callback) {
        setCallback(callback);
    }

    public void setCallback(VehicleDataSourceCallbackInterface callback) {
        mCallback = callback;
    }

    protected void handleMessage(String name, double value) {
        if(mCallback != null) {
            mCallback.receive(name, value);
        }
    }

    protected void handleMessage(String name, String value) {
        if(mCallback != null) {
            mCallback.receive(name, value);
        }
    }
}
