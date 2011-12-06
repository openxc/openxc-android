package com.openxc.remote.sources.manual;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openxc.remote.sources.AbstractVehicleDataSourceCallback;
import com.openxc.remote.sources.VehicleDataSourceCallbackInterface;

public class ManualVehicleDataSourceTest {
    ManualVehicleDataSource source;
    VehicleDataSourceCallbackInterface callback;
    boolean receivedCallback;

    @Before
    public void setUp() {
        receivedCallback = false;
        source = new ManualVehicleDataSource();
        callback = new AbstractVehicleDataSourceCallback() {
            public void receive(String name, Double value, Double event) {
            }

            public void receive(String name, Double value) {
                receivedCallback = true;
            }

            public void receive(String name, Boolean value) {
                receivedCallback = true;
            }

            public void receive(String name, String value) {
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
