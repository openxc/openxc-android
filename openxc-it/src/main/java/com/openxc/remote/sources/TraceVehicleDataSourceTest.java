package com.openxc.remote.sources;

import java.io.InputStream;

import java.lang.InterruptedException;

import java.net.MalformedURLException;
import java.net.URL;

import com.openxc.R;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class TraceVehicleDataSourceTest extends AndroidTestCase {
    InputStream filename;
    InputStream malformedFilename;
    TraceVehicleDataSource source;
    VehicleDataSourceCallbackInterface callback;
    boolean receivedNumericalCallback;
    boolean receivedStateCallback;
    double receivedNumber;
    String receivedState;

    @Override
    protected void setUp() {
        filename = getContext().getResources().openRawResource(
                R.raw.tracejson);
        malformedFilename = getContext().getResources().openRawResource(
                R.raw.tracetxt);
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

    @SmallTest
    public void testPlaybackFile() throws InterruptedException,
            VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, filename);
        source.run();
        assertTrue(receivedNumericalCallback);
        assertTrue(receivedStateCallback);
        assertEquals(receivedNumber, 42.0);
        assertEquals(receivedState, "MyState");
    }

    @SmallTest
    public void testMalformedJson() throws InterruptedException ,
            VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, malformedFilename);
        source.run();
        assertFalse(receivedNumericalCallback);
    }

    @SmallTest
    public void testMissingFile() throws MalformedURLException,
            InterruptedException, VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, new URL("file://foo"));
        source.run();
        assertFalse(receivedNumericalCallback);
    }

    @SmallTest
    public void testConstructWithCallbackAndFile()
            throws VehicleDataSourceException {
        source = new TraceVehicleDataSource(callback, filename);
    }
}
