package com.openxc.interfaces.bluetooth;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.openxc.sources.DataSourceException;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
/* This test has to live in the openxc-it package because you can't access
 * Android resources like R.* from robolectric tests in a library project.
 */
public class BluetoothVehicleInterfaceTest {
    String macAddress = "00:1C:B3:09:85:15";
    BluetoothVehicleInterface source;

    @After
    public void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
    }

    @Test
    public void testValidaddress() throws DataSourceException {
        source = new BluetoothVehicleInterface(Robolectric.application, macAddress);
    }

    @Test
    public void testResourceMatching() throws DataSourceException {
        source = new BluetoothVehicleInterface(Robolectric.application, macAddress);
        assertFalse(source.setResource(macAddress));
    }

    @Test
    public void testResourceDifferent() throws DataSourceException {
        String anotherMac = "01:00:00:00:00:00";
        source = new BluetoothVehicleInterface(Robolectric.application, macAddress);
        // TODO it has to be connected first before this will return true, need
        // a better test
        // assertTrue(source.setResource(anotherMac));
    }
}
