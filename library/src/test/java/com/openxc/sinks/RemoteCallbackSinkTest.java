package com.openxc.sinks;

import android.os.SystemClock;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class RemoteCallbackSinkTest {
    RemoteCallbackSink notifier;
    VehicleServiceListener listener;
    String messageId = "the_measurement";
    String receivedId = null;

    @Before
    public void setUp() {
        notifier = new RemoteCallbackSink();
        listener = new VehicleServiceListener.Stub() {
            @Override
            public void receive(VehicleMessage value) {
                receivedId = ((NamedVehicleMessage)value).getName();
            }
        };
    }

    @Test
    public void testRegister() {
        assertEquals(0, notifier.getListenerCount());
        notifier.register(listener);
        assertEquals(1, notifier.getListenerCount());
    }

    @Test
    public void testUnregisterInvalid() {
        // this just shouldn't explode, it should ignore it...or should it?
        // failing silently is usually a bad thing
        assertEquals(0, notifier.getListenerCount());
        notifier.unregister(listener);
        assertEquals(0, notifier.getListenerCount());
    }

    @Test
    public void testUnregisterValid() {
        notifier.register(listener);
        assertEquals(1, notifier.getListenerCount());
        notifier.unregister(listener);
        assertEquals(0, notifier.getListenerCount());
    }

    @Test
    public void testReceiveCorrectId() throws DataSinkException {
        notifier.register(listener);
        assertNull(receivedId);
        SimpleVehicleMessage message = new SimpleVehicleMessage(messageId, 1);
        notifier.receive(message);
        SystemClock.sleep(50);
        assertNotNull(receivedId);
        assertEquals(receivedId, messageId);
    }
}
