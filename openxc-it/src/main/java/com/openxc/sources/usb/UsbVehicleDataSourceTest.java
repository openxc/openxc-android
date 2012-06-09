package com.openxc.sources.usb;

import java.lang.InterruptedException;

import java.net.URI;
import java.net.URISyntaxException;

import com.openxc.remote.RawMeasurement;

import com.openxc.sources.usb.UsbVehicleDataSource;
import com.openxc.sources.SourceCallback;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

import junit.framework.Assert;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class UsbVehicleDataSourceTest extends AndroidTestCase {
    URI deviceUri;
    URI malformedDeviceUri;
    URI incorrectSchemeUri;
    UsbVehicleDataSource source;
    SourceCallback callback;
    Thread thread;

    @Override
    protected void setUp() {
        try {
            deviceUri = new URI("usb://04d8/0053");
            malformedDeviceUri = new URI("usb://04d8");
            incorrectSchemeUri = new URI("file://04d8");
        } catch(URISyntaxException e) {
            Assert.fail("Couldn't construct resource URIs: " + e);
        }

        callback = new SourceCallback() {
            public void receive(RawMeasurement measurement) {
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
    public void testDefaultDevice() throws DataSourceException {
        source = new UsbVehicleDataSource(callback, getContext());
    }

    @SmallTest
    public void testCustomDevice() throws DataSourceException {
        source = new UsbVehicleDataSource(callback, getContext(),
                deviceUri);
    }

    @SmallTest
    public void testMalformedUri() throws DataSourceException {
        try {
            new UsbVehicleDataSource(callback, getContext(),
                    malformedDeviceUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @SmallTest
    public void testUriWithBadScheme() throws DataSourceException {
        try {
            new UsbVehicleDataSource(callback, getContext(),
                    incorrectSchemeUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }
}
