package com.openxc.sinks;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceListener;

public class RemoteCallbackSinkTest extends AndroidTestCase {
    RemoteCallbackSink notifier;
    VehicleServiceListener listener;
    String messageId = "the_measurement";
    String receivedId = null;

    @Override
    public void setUp() {
        notifier = new RemoteCallbackSink();
        listener = new VehicleServiceListener.Stub() {
            public void receive(VehicleMessage value) {
                receivedId = ((NamedVehicleMessage)value).getName();
            }
        };
    }

    @SmallTest
    public void testRegister() {
        assertEquals(0, notifier.getListenerCount());
        notifier.register(listener);
        assertEquals(1, notifier.getListenerCount());
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
        assertEquals(1, notifier.getListenerCount());
        notifier.unregister(listener);
        assertEquals(0, notifier.getListenerCount());
    }

    @SmallTest
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
