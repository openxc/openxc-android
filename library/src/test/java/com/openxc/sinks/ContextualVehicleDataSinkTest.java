package com.openxc.sinks;

import android.content.Context;

import org.junit.runner.RunWith;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import com.openxc.messages.VehicleMessage;

@RunWith(RobolectricTestRunner.class)
public class ContextualVehicleDataSinkTest {
    @Test
    public void testConstructWithContext() {
        TestContextualSink sink = new TestContextualSink(
                RuntimeEnvironment.application);
        assertThat(sink.getContextSpy(), notNullValue());
    }

    private class TestContextualSink extends ContextualVehicleDataSink {
        public TestContextualSink(Context context) {
            super(context);
        }

        public Context getContextSpy() {
            return getContext();
        }

        @Override
        public void receive(VehicleMessage message) { }

        @Override
        public void stop() { }
    }
}
