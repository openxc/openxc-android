package com.openxc.interfaces.bluetooth;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.openxc.sources.DataSourceException;

public class BluetoothVehicleInterfaceTest extends AndroidTestCase {
    String macAddress = "00:00:00:00:00:00";
    BluetoothVehicleInterface source;

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
        super.tearDown();
    }

    @SmallTest
    public void testValidaddress() throws DataSourceException {
        source = new BluetoothVehicleInterface(getContext(), macAddress);
    }

    @SmallTest
    public void testResourceMatching() throws DataSourceException {
        source = new BluetoothVehicleInterface(getContext(), macAddress);
        assertFalse(source.setResource(macAddress));
    }

    @SmallTest
    public void testResourceDifferent() throws DataSourceException {
        String anotherMac = "01:00:00:00:00:00";
        source = new BluetoothVehicleInterface(getContext(), macAddress);
        assertTrue(source.setResource(anotherMac));
    }
}
