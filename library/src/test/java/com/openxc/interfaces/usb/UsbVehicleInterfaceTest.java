package com.openxc.interfaces.usb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

import android.content.Context;

@RunWith(RobolectricTestRunner.class)
public class UsbVehicleInterfaceTest {
    String deviceUri = "usb://04d8/0053";
    String malformedDeviceUri = "usb://04d8";
    String incorrectSchemeUri = "file://04d8";
    UsbVehicleInterface source;

    @After
    public void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
    }

    private Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Test
    public void testDefaultDevice() throws DataSourceException {
        // TODO need a ShadowUsBManager in Robolectric before we can run this
        // source = new UsbVehicleInterface(getContext());
    }

    @Test
    public void testCustomDevice() throws DataSourceException {
        UsbVehicleInterface.createUri(deviceUri);
    }

    @Test
    public void testMalformedUri() throws DataSourceException {
        try {
            source = new UsbVehicleInterface(getContext(), malformedDeviceUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @Test
    public void testUriWithBadScheme() throws DataSourceException {
        try {
            source = new UsbVehicleInterface(getContext(), incorrectSchemeUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @Test
    public void testResourceMatchingDefault() throws DataSourceException {
        // TODO need a ShadowUsBManager in Robolectric before we can run this
        // source = new UsbVehicleInterface(getContext());
        // assertFalse(source.setResource(null));
    }

    // TODO UsbVehicleInterface - test that receive throws an exception if not
    //      connected
}
