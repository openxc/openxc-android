package com.openxc.sinks;

import org.junit.Test;
import org.junit.Before;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceListener;

@Config(emulateSdk = 18, manifest = Config.NONE)
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
        notifier.receive(new SimpleVehicleMessage(messageId, 1));
        try {
            Thread.sleep(50);
        } catch(InterruptedException e) {}
        assertTrue(notifier.containsNamedMessage(messageId));
        assertNotNull(receivedId);
        assertEquals(receivedId, messageId);
    }
}
