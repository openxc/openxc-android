package com.openxc.interfaces.usb;

import junit.framework.Assert;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

public class UsbVehicleInterfaceTest extends AndroidTestCase {
    String deviceUri = "usb://04d8/0053";
    String malformedDeviceUri = "usb://04d8";
    String incorrectSchemeUri = "file://04d8";
    UsbVehicleInterface source;

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
        super.tearDown();
    }

    @SmallTest
    public void testDefaultDevice() throws DataSourceException {
        source = new UsbVehicleInterface(getContext());
    }

    @SmallTest
    public void testCustomDevice() throws DataSourceException {
        source = new UsbVehicleInterface(getContext(), deviceUri);
    }

    @SmallTest
    public void testMalformedUri() throws DataSourceException {
        try {
            source = new UsbVehicleInterface(getContext(), malformedDeviceUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @SmallTest
    public void testUriWithBadScheme() throws DataSourceException {
        try {
            source = new UsbVehicleInterface(getContext(), incorrectSchemeUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @SmallTest
    public void testResourceMatchingDefault() throws DataSourceException {
        source = new UsbVehicleInterface(getContext());
        assertFalse(source.setResource(null));
    }
}
