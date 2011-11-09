package com.openxc.remote.sources;

public interface VehicleDataSourceCallbackInterface {
    public void receive(String name, double value);
    public void receive(String name, String value);
}
