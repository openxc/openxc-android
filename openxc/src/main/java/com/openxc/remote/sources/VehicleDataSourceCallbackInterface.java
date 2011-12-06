package com.openxc.remote.sources;

public interface VehicleDataSourceCallbackInterface {
    public void receive(String name, Double value);
    public void receive(String name, Double value, Double event);
    public void receive(String name, Integer value);
    public void receive(String name, Boolean value);
    public void receive(String name, String value);
    public void receive(String name, String value, String event);
}
