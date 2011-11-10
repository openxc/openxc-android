package com.openxc.remote.sources;

public interface VehicleDataSourceInterface extends Runnable {
    public void setCallback(VehicleDataSourceCallbackInterface callback);
    public void stop();
}
