package com.openxc.remote.sinks;

import com.openxc.remote.RawMeasurement;

public interface VehicleDataSink {
    public void receive(String measurementId, Object value, Object event);
    public void receive(String measurementId, RawMeasurement measurement);
    public void stop();
}
