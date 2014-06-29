package com.openxc.sinks;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;

import com.openxc.TestUtils;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.KeyMatcher;
import com.openxc.messages.ExactKeyMatcher;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MessageListenerSinkTest {
    MessageListenerSink sink;
    SpyListener listener = new SpyListener();

    @Before
    public void setUp() {
        sink = new MessageListenerSink();
    }

    @Test
    public void listenerRecievesMessage() throws DataSinkException {
        SimpleVehicleMessage message = new SimpleVehicleMessage("foo", "bar");
        sink.register(ExactKeyMatcher.buildExactMatcher(message), listener);
        sink.receive(message);
        // TODO would like to not have something as unreliable as a delay here,
        // but we are waiting on the backgroud thread in
        // AbstractQueuedCallbackSink to propagate the data
        TestUtils.pause(20);
        assertThat(listener.received, notNullValue());
        assertEquals(listener.received, message);
    }

    private class SpyListener implements VehicleMessage.Listener {
        public VehicleMessage received;

        public void receive(VehicleMessage message) {
            received = message;
        }
    };
}
