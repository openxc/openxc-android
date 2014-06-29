package com.openxc.sinks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.openxc.messages.SimpleVehicleMessage;

public class BaseVehicleDataSinkTest {
    BaseVehicleDataSink sink;

    @Before
    public void setUp() {
        sink = new BaseVehicleDataSink();
    }

    @Test
    public void storesNamedMessageLocally() throws DataSinkException {
        String name = "foo";
        SimpleVehicleMessage message = new SimpleVehicleMessage(name, "value");
        sink.receive(message);
        assertTrue(sink.containsKey(message.getKey()));
        assertThat(sink.get(message.getKey()).asSimpleMessage(),
                equalTo(message));
    }
}
