package com.openxc.sinks;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.VehicleServiceListenerInterface;

import android.test.AndroidTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class RemoteCallbackSinkTest extends AndroidTestCase {
    Map<String, RawMeasurement> measurements;
    RemoteCallbackSink notifier;
    VehicleServiceListenerInterface listener;
    String measurementId = "the_measurement";
    String receivedId = null;

    @Before
    public void setUp() {
        // TODO what are the contractual guarantees that this class says about
        // this measurements map?
        measurements = new HashMap<String, RawMeasurement>();
        notifier = new RemoteCallbackSink();
        notifier.setMeasurements(measurements);
        listener = new VehicleServiceListenerInterface.Stub() {
            public void receive(String measurementId, RawMeasurement value) {
                receivedId = measurementId;
            }
        };
    }

    @SmallTest
    public void testRegister() {
        assertTrue(notifier.getListenerCount() == 0);
        notifier.register(listener);
        assertTrue(notifier.getListenerCount() == 1);
    }

    @SmallTest
    public void testUnregisterInvalid() {
        // this just shouldn't explode, it should ignore it...or should it?
        // failing silently is usually a bad thing
        assertEquals(0, notifier.getListenerCount());
        notifier.unregister(listener);
        assertEquals(0, notifier.getListenerCount());
    }

    @SmallTest
    public void testUnregisterValid() {
        notifier.register(listener);
        assertTrue(notifier.getListenerCount() == 1);
        notifier.unregister(listener);
        assertTrue(notifier.getListenerCount() == 0);
    }

    @SmallTest
    public void testReceiveCorrectId() {
        notifier.register(listener);
        assertTrue(receivedId == null);
        notifier.receive(measurementId, new RawMeasurement(1));
        assertNotNull(receivedId);
        assertTrue(receivedId.equals(measurementId));
    }
}
