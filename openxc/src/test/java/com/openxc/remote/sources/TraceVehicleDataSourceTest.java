package com.openxc.remote.sources;

import java.lang.InterruptedException;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TraceVehicleDataSourceTest {
    TraceVehicleDataSource source;
    VehicleDataSourceCallbackInterface callback;
    boolean receivedNumericalCallback;
    boolean receivedStateCallback;
    final URL filename = this.getClass().getResource("/trace.json");
    final URL malformedFilename = this.getClass().getResource("/trace.txt");

    @Before
    public void setUp() {
        source = new TraceVehicleDataSource();
        callback = new VehicleDataSourceCallbackInterface() {
            public void receive(String name, double value) {
                receivedNumericalCallback = true;
            }

            public void receive(String name, String value) {
                receivedStateCallback = true;
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
    public void testSetFile() {
    }

    private void runSourceToCompletion(VehicleDataSourceInterface source)
            throws InterruptedException {
        Thread thread = new Thread(source);
        thread.run();
        thread.join();
    }

    @Test
    public void testPlaybackFile() throws InterruptedException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, filename);
        runSourceToCompletion(source);
        assert(receivedNumericalCallback);
        assert(receivedStateCallback);
    }

    @Test
    public void testMalformedJson() throws InterruptedException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, malformedFilename);
        runSourceToCompletion(source);
        assert(!receivedNumericalCallback);
    }

    @Test
    public void testMissingFile() throws MalformedURLException,
            InterruptedException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, new URL("/foo"));
        runSourceToCompletion(source);
        assert(!receivedNumericalCallback);
    }

    @Test
    public void testConstructWithCallback() {
        source = new TraceVehicleDataSource(callback);
    }

    @Test
    public void testConstructWithCallbackAndFile() {
        source = new TraceVehicleDataSource(callback, filename);
    }
}
