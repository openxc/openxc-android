package com.openxc.sinks;

import com.openxc.sinks.BaseVehicleDataSink;
import com.openxc.messages.VehicleMessage;

public class TestSink extends BaseVehicleDataSink {
    public boolean received = false;

    public void receive(VehicleMessage measurement) {
        received = true;
    }
}
