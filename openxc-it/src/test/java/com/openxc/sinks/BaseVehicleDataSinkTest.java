package com.openxc.sinks;

import junit.framework.TestCase;

import static org.mockito.Mockito.*;

import com.openxc.remote.RawMeasurement;

public class BaseVehicleDataSinkTest extends TestCase {
    BaseVehicleDataSink sink;

    @Override
    public void setUp() {
        sink = spy(new BaseVehicleDataSink());
    }

    public void testReceiveValueOnly() {
        sink.receive("measurement", "value");
        verify(sink).receive(eq("measurement"), any(RawMeasurement.class));
    }

    public void testReceiveEvented() {
        sink.receive("measurement", "value", "event");
        verify(sink).receive(eq("measurement"), any(RawMeasurement.class));
    }

    public void testReceiveRawMeasurement() {
        sink.receive("measurement", new RawMeasurement("value"));
    }

    public void testStop() {
        sink.stop();
    }
}
