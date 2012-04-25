package com.openxc.remote.sinks;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import static org.mockito.Mockito.*;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import android.os.RemoteException;

public class MeasurementNotifierSinkTest extends AndroidTestCase {
    Map<String, RawMeasurement> measurements;
    MeasurementNotifierSink notifier;
    RemoteVehicleServiceListenerInterface listener;
    String measurementId = "the_measurement";

    @Override
    public void setUp() {
        // TODO what are the contractual guarantees that this class says about
        // this measurements map?
        measurements = new HashMap<String, RawMeasurement>();
        notifier = new MeasurementNotifierSink(measurements);
        listener = mock(RemoteVehicleServiceListenerInterface.class);
    }

    @SmallTest
    public void testRegister() {
        assertThat(notifier.getListenerCount(), equalTo(0));
        notifier.register(measurementId, listener);
        assertThat(notifier.getListenerCount(), equalTo(1));
    }

    @SmallTest
    public void testUnregisterInvalid() {
        // this just shouldn't explode, it should ignore it...or should it?
        // failing silently is usually a bad thing
        assertThat(notifier.getListenerCount(), equalTo(0));
        notifier.unregister(measurementId, listener);
        assertThat(notifier.getListenerCount(), equalTo(0));
    }

    @SmallTest
    public void testUnregisterValid() {
        notifier.register(measurementId, listener);
        assertThat(notifier.getListenerCount(), equalTo(1));
        notifier.unregister(measurementId, listener);
        assertThat(notifier.getListenerCount(), equalTo(0));
    }

    @SmallTest
    public void testReceiveCorrectId() throws RemoteException {
        notifier.register(measurementId, listener);
        verify(listener, never()).receive(eq(measurementId), any(RawMeasurement.class));
        notifier.receive(measurementId, new RawMeasurement(1));
        verify(listener).receive(eq(measurementId), any(RawMeasurement.class));
    }

    @SmallTest
    public void testNoReceiveAnotherId() throws RemoteException {
        notifier.register(measurementId, listener);
        verify(listener, never()).receive(eq(measurementId), any(RawMeasurement.class));
        notifier.receive("another_measurement", new RawMeasurement(1));
        verify(listener, never()).receive(eq(measurementId), any(RawMeasurement.class));
    }
}
