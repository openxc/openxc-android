package com.openxc.interfaces.network;

import junit.framework.Assert;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

public class NetworkVehicleInterfaceTest extends AndroidTestCase {
    String goodUri = "//192.168.1.1:4000";
    String missingPortUri = "//192.168.1.1";
    String incorrectSchemeUri = "file://192.168.1.1:4000";
    String missingPrefixUri = "192.168.2.2:5000";
    NetworkVehicleInterface source;

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
        super.tearDown();
    }

    @SmallTest
    public void testValidUri() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), goodUri);
    }

    @SmallTest
    public void testValidateResource() {
        assertTrue(NetworkVehicleInterface.validateResource(goodUri));
        assertFalse(NetworkVehicleInterface.validateResource(missingPortUri));
        assertFalse(NetworkVehicleInterface.validateResource(incorrectSchemeUri));
        assertTrue(NetworkVehicleInterface.validateResource(missingPrefixUri));
    }

    @SmallTest
    public void testResourceMatching() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), goodUri);
        assertFalse(source.setResource(goodUri));
    }

    @SmallTest
    public void testResourceMatchingMassaged() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), missingPrefixUri);
        assertFalse(source.setResource(missingPrefixUri));
    }

    @SmallTest
    public void testResourceChanged() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), goodUri);
        assertTrue(source.setResource(missingPrefixUri));
    }

    @SmallTest
    public void testMalformedUri() throws DataSourceException {
        try {
            source = new NetworkVehicleInterface(getContext(), missingPortUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @SmallTest
    public void testUriWithBadScheme() throws DataSourceException {
        try {
            source = new NetworkVehicleInterface(getContext(),
                    incorrectSchemeUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @SmallTest
    public void testMissingPrefix() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), missingPrefixUri);
    }
}
