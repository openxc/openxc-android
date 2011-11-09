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

    protected abstract void handleMessage(String name, double value);
    protected abstract void handleMessage(String name, String value);
}
