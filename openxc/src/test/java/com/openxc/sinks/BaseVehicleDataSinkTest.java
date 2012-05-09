package com.openxc.sinks;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import com.openxc.remote.RawMeasurement;

public class BaseVehicleDataSinkTest {
    BaseVehicleDataSink sink;

    @Before
    public void setUp() {
        sink = spy(new BaseVehicleDataSink());
    }

    @Test
    public void testReceiveValueOnly() {
        sink.receive("measurement", "value");
        verify(sink).receive(eq("measurement"), any(RawMeasurement.class));
    }

    @Test
    public void testReceiveEvented() {
        sink.receive("measurement", "value", "event");
        verify(sink).receive(eq("measurement"), any(RawMeasurement.class));
    }

    @Test
    public void testReceiveRawMeasurement() {
        sink.receive("measurement", new RawMeasurement("value"));
    }

    @Test
    public void testStop() {
        sink.stop();
    }
}
