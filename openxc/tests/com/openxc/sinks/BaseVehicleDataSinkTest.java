package com.openxc.sinks;

import static org.mockito.Mockito.spy;

import junit.framework.TestCase;

import com.openxc.messages.SimpleVehicleMessage;

public class BaseVehicleDataSinkTest extends TestCase {
    BaseVehicleDataSink sink;

    @Override
    public void setUp() {
        sink = spy(new BaseVehicleDataSink());
    }

    public void testReceiveVehicleMessage() throws DataSinkException {
        sink.receive(new SimpleVehicleMessage("measurement_type", "value"));
        // TODO not testing anything!
    }

    public void testStop() {
        sink.stop();
    }
}
