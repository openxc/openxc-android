package com.openxc.remote.sources.usb;

import java.lang.InterruptedException;

import java.net.URI;
import java.net.URISyntaxException;

import com.openxc.remote.sources.AbstractVehicleDataSourceCallback;

import com.openxc.remote.sources.usb.UsbVehicleDataSource;

import com.openxc.remote.sources.VehicleDataSourceCallbackInterface;
import com.openxc.remote.sources.VehicleDataSourceException;
import com.openxc.remote.sources.VehicleDataSourceInterface;
import com.openxc.remote.sources.VehicleDataSourceResourceException;

import junit.framework.Assert;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class UsbVehicleDataSourceTest extends AndroidTestCase {
    URI deviceUri;
    URI malformedDeviceUri;
    URI incorrectSchemeUri;
    UsbVehicleDataSource source;
    VehicleDataSourceCallbackInterface callback;
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

        callback = new AbstractVehicleDataSourceCallback() {
            public void receive(String name, Object value) {
            }

            public void receive(String name, Object value, Object event) {
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

    private void startSource(VehicleDataSourceInterface source) {
        thread = new Thread(source);
        thread.start();
        try {
            Thread.sleep(100);
        } catch(InterruptedException e){ }
    }


    @SmallTest
    public void testDefaultDevice() throws VehicleDataSourceException {
        source = new UsbVehicleDataSource(getContext(), callback);
        startSource(source);
    }

    @SmallTest
    public void testCustomDevice() throws VehicleDataSourceException {
        source = new UsbVehicleDataSource(getContext(), callback,
                deviceUri);
        startSource(source);
    }

    @SmallTest
    public void testMalformedUri() throws VehicleDataSourceException {
        try {
            new UsbVehicleDataSource(getContext(), callback,
                    malformedDeviceUri);
        } catch(VehicleDataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a VehicleDataSourceResourceException");
    }

    @SmallTest
    public void testUriWithBadScheme() throws VehicleDataSourceException {
        try {
            new UsbVehicleDataSource(getContext(), callback,
                    incorrectSchemeUri);
        } catch(VehicleDataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a VehicleDataSourceResourceException");
    }
}
