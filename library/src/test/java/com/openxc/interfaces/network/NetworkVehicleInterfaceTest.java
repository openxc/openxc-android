package com.openxc.interfaces.network;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

@RunWith(RobolectricTestRunner.class)
public class NetworkVehicleInterfaceTest {
    String goodUri = "//192.168.1.1:4000";
    String missingPortUri = "//192.168.1.1";
    String incorrectSchemeUri = "file://192.168.1.1:4000";
    String missingPrefixUri = "192.168.2.2:5000";
    NetworkVehicleInterface source;

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
    public void testValidUri() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), goodUri);
    }

    @Test
    public void testValidateResource() {
        assertTrue(NetworkVehicleInterface.validateResource(goodUri));
        assertFalse(NetworkVehicleInterface.validateResource(missingPortUri));
        assertFalse(NetworkVehicleInterface.validateResource(incorrectSchemeUri));
        assertTrue(NetworkVehicleInterface.validateResource(missingPrefixUri));
    }

    @Test
    public void testResourceMatching() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), goodUri);
        assertFalse(source.setResource(goodUri));
    }

    @Test
    public void testResourceMatchingMassaged() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), missingPrefixUri);
        assertFalse(source.setResource(missingPrefixUri));
    }

    @Test
    public void testResourceChanged() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), goodUri);
        assertTrue(source.setResource(missingPrefixUri));
    }

    @Test
    public void testValidateInvalidPort() throws DataSourceException {
        assertFalse(NetworkVehicleInterface.validateResource("http://localhost:70000"));
    }

    @Test
    public void testInvalidPort() throws DataSourceException {
        try {
            source = new NetworkVehicleInterface(getContext(), "http://localhost:70000");
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @Test
    public void testMalformedUri() throws DataSourceException {
        try {
            source = new NetworkVehicleInterface(getContext(), missingPortUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @Test
    public void testUriWithBadScheme() throws DataSourceException {
        try {
            source = new NetworkVehicleInterface(getContext(),
                    incorrectSchemeUri);
        } catch(DataSourceResourceException e) {
            return;
        }
        Assert.fail("Expected a DataSourceResourceException");
    }

    @Test
    public void testMissingPrefix() throws DataSourceException {
        source = new NetworkVehicleInterface(getContext(), missingPrefixUri);
    }
}
