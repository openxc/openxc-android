package com.openxc.remote.sources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManualVehicleDataSourceTest {
    ManualVehicleDataSource source;
    VehicleDataSourceCallbackInterface callback;
    boolean receivedCallback;

    @Before
    public void setUp() {
        receivedCallback = false;
        source = new ManualVehicleDataSource();
        callback = new VehicleDataSourceCallbackInterface() {
            public void receive(String name, double value) {
                receivedCallback = true;
            }

            public void receive(String name, String value) {
                receivedCallback = true;
            }
        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSetCallback() {
        source.setCallback(callback);
    }

    @Test
    public void testTriggerCallback() {
        receivedCallback = false;
        source.setCallback(callback);
        source.trigger("test", 42);
        assert(receivedCallback);
    }

    @Test
    public void testConstructWithCallback() {
        source = new ManualVehicleDataSource(callback);
    }
}
