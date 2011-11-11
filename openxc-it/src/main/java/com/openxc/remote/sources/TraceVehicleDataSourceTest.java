package com.openxc.remote.sources;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.openxc.R;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class TraceVehicleDataSourceTest extends AndroidTestCase {
    URI traceUri;
    URI malformedTraceUri;
    TraceVehicleDataSource source;
    Thread thread;
    VehicleDataSourceCallbackInterface callback;
    boolean receivedNumericalCallback;
    boolean receivedStateCallback;
    double receivedNumber;
    String receivedState;

    @Override
    protected void setUp() {
        try {
            traceUri = new URI("file:///data/data/com.openxc/trace.json");
            malformedTraceUri = new URI("file:///data/data/com.openxc/malformed-trace.json");
        } catch(URISyntaxException e) { }

        try {
            FileUtils.copyInputStreamToFile(getContext().getResources().openRawResource(
                        R.raw.tracejson), new File(traceUri));
            FileUtils.copyInputStreamToFile(getContext().getResources().openRawResource(
                        R.raw.tracetxt), new File(malformedTraceUri));
        } catch(IOException e) {}

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

    @Override
    protected void tearDown() {
        if(source != null) {
            source.stop();
        }
        if(thread != null) {
            try {
                thread.join();
            } catch(InterruptedException e) {}
        }
    }

    @SmallTest
    public void testPlaybackFile() throws InterruptedException,
            VehicleDataSourceException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback, traceUri);
        thread = new Thread(source);
        thread.start();
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
        source = new TraceVehicleDataSource(callback, malformedTraceUri);
        thread = new Thread(source);
        thread.start();
        assertFalse(receivedNumericalCallback);
    }

    @SmallTest
    public void testMissingFile() throws MalformedURLException,
            InterruptedException, VehicleDataSourceException,
            URISyntaxException {
        receivedNumericalCallback = false;
        receivedStateCallback = false;
        source = new TraceVehicleDataSource(callback,
                new URL("file:///foo").toURI());
        thread = new Thread(source);
        thread.start();
        assertFalse(receivedNumericalCallback);
    }

    @SmallTest
    public void testConstructWithCallbackAndFile()
            throws VehicleDataSourceException {
        source = new TraceVehicleDataSource(callback, traceUri);
    }
}
