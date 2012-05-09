package com.openxc.sinks;

import java.util.Map;

import com.openxc.remote.RawMeasurement;
import com.openxc.VehicleDataEndpoint;

public interface VehicleDataSink extends VehicleDataEndpoint {
    public void receive(String measurementId, Object value, Object event);
    public void receive(String measurementId, RawMeasurement measurement);
    public void setMeasurements(Map<String, RawMeasurement> measurements);
    public void stop();
}
