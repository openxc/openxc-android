package com.openxc.remote.sinks;

import com.openxc.remote.RawMeasurement;

import com.openxc.remote.VehicleDataEndpoint;

public interface VehicleDataSink extends VehicleDataEndpoint {
    public void receive(String measurementId, Object value, Object event);
    public void receive(String measurementId, RawMeasurement measurement);
    public void stop();
}
