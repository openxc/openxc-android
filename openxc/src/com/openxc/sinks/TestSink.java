package com.openxc.sinks;

import com.openxc.messages.VehicleMessage;

public class TestSink implements VehicleDataSink {
    public boolean received = false;

    @Override
    public void receive(VehicleMessage measurement) {
        received = true;
    }

    @Override
    public void stop() { }
}
