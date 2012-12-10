package com.openxc.sources.trace;

import java.lang.InterruptedException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.openxc.remote.RawMeasurement;

import com.openxc.sources.SourceCallback;
import com.openxc.sources.DataSourceException;

import com.openxc.TestUtils;

import android.test.AndroidTestCase;

import com.openxc.R;

import android.test.suitebuilder.annotation.SmallTest;

public class TraceVehicleDataSourceTest extends AndroidTestCase {
    URI traceUri;
    URI malformedTraceUri;
    TraceVehicleDataSource source;
    Thread thread;
    SourceCallback callback;
    boolean receivedNumericalCallback;
    boolean receivedBooleanCallback;;

    @Override
    protected void setUp() {
        traceUri = TestUtils.copyToStorage(getContext(), R.raw.tracejson,
                "trace.json");
        malformedTraceUri = TestUtils.copyToStorage(getContext(),
                R.raw.tracetxt, "malformed-trace.json");
        callback = new SourceCallback() {
            public void receive(RawMeasurement measurement) {
                if(measurement.getValue().getClass() == Boolean.class) {
                    receivedBooleanCallback = true;
                } else if(measurement.getValue().getClass() == Double.class)  {
                    receivedNumericalCallback = true;
                }
            }
        };
    }

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
        if(thread != null) {
            try {
                thread.join();
            } catch(InterruptedException e) {}
        }
        super.tearDown();
    }

    private void startTrace(TraceVehicleDataSource source) {
        thread = new Thread(source);
        thread.start();
        try {
            Thread.sleep(300);
        } catch(InterruptedException e){ }
    }

    @SmallTest
    public void testPlaybackFile() throws InterruptedException,
            DataSourceException {
        receivedNumericalCallback = false;
        receivedBooleanCallback = false;
        source = new TraceVehicleDataSource(callback, getContext(), traceUri);
        startTrace(source);
        assertTrue(receivedNumericalCallback);
        assertTrue(receivedBooleanCallback);
    }

    @SmallTest
    public void testMalformedJson() throws InterruptedException ,
            DataSourceException {
        receivedNumericalCallback = false;
        receivedBooleanCallback = false;
        source = new TraceVehicleDataSource(callback, getContext(),
                malformedTraceUri);
        startTrace(source);
        assertFalse(receivedNumericalCallback);
        source.stop();
    }

    @SmallTest
    public void testMissingFile() throws MalformedURLException,
            InterruptedException, DataSourceException,
            URISyntaxException {
        receivedNumericalCallback = false;
        receivedBooleanCallback = false;
        source = new TraceVehicleDataSource(callback, getContext(),
                new URL("file:///foo").toURI());
        startTrace(source);
        assertFalse(receivedNumericalCallback);
    }

    @SmallTest
    public void testConstructWithCallbackAndFile()
            throws DataSourceException {
        source = new TraceVehicleDataSource(callback, getContext(), traceUri);
    }
}
