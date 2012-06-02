package com.openxc.sinks;

import java.util.HashMap;
import java.util.Map;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

    @Override
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
        assertThat(notifier.getListenerCount(), equalTo(0));
        notifier.register(listener);
        assertThat(notifier.getListenerCount(), equalTo(1));
    }

    @SmallTest
    public void testUnregisterInvalid() {
        // this just shouldn't explode, it should ignore it...or should it?
        // failing silently is usually a bad thing
        assertThat(notifier.getListenerCount(), equalTo(0));
        notifier.unregister(listener);
        assertThat(notifier.getListenerCount(), equalTo(0));
    }

    @SmallTest
    public void testUnregisterValid() {
        notifier.register(listener);
        assertThat(notifier.getListenerCount(), equalTo(1));
        notifier.unregister(listener);
        assertThat(notifier.getListenerCount(), equalTo(0));
    }

    @SmallTest
    public void testReceiveCorrectId() {
        notifier.register(listener);
        assertThat(receivedId, equalTo(null));
        notifier.receive(measurementId, new RawMeasurement(1));
        assertThat(receivedId, equalTo(measurementId));
    }
}
