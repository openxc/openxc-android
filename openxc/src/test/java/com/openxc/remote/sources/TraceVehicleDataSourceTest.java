package com.openxc.remote.sources;

import java.lang.InterruptedException;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TraceVehicleDataSourceTest {
    final URL filename = this.getClass().getResource("/trace.json");
    final URL malformedFilename = this.getClass().getResource("/trace.txt");
    TraceVehicleDataSource source;
    VehicleDataSourceCallbackInterface callback;
    boolean receivedNumericalCallback;
    boolean receivedStateCallback;
    double receivedNumber;
    String receivedState;

    @Before
    public void setUp() {
        callback = new VehicleDataSourceCallbackInterface() {
            public void receive(String name, double value) {
                receivedNumericalCallback = true;
                receivedNumber = value;
            }

            public void receive(String name, String value) {
                receivedStateCallback = true;
                receivedState = value;
            }
        };
    }

    @After
    public void tearDown() {
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
    public void testPlaybackFile() throws InterruptedException,
            VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, filename);
        runSourceToCompletion(source);
        assert(receivedNumericalCallback);
        assert(receivedStateCallback);
        assertEquals(receivedNumber, 42);
        assertEquals(receivedState, "MyState");
    }

    @Test
    public void testMalformedJson() throws InterruptedException ,
            VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, malformedFilename);
        runSourceToCompletion(source);
        assert(!receivedNumericalCallback);
    }

    @Test
    public void testMissingFile() throws MalformedURLException,
            InterruptedException, VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, new URL("file://foo"));
        runSourceToCompletion(source);
        assert(!receivedNumericalCallback);
    }

    @Test
    public void testConstructWithCallbackAndFile()
            throws VehicleDataSourceException {
        source = new TraceVehicleDataSource(callback, filename);
    }
}
