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

    public void testReceiveRawMeasurement() throws DataSinkException {
        sink.receive(new RawMeasurement("measurement_type", "value"));
    }

    public void testStop() {
        sink.stop();
    }
}
