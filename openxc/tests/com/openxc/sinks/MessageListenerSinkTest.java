package com.openxc.sinks;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.messages.KeyedMessage;
import com.openxc.messages.ExactKeyMatcher;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.BaseMeasurement;

@Config(emulateSdk = 18, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class MessageListenerSinkTest {
    MessageListenerSink sink;
    SpyListener listener = new SpyListener();
    VehicleSpeed speedReceived;

    @Before
    public void setUp() {
        sink = new MessageListenerSink();
    }

    @After
    public void tearDown() {
        sink.stop();
    }

    @Test
    public void nonKeyedIgnored() throws DataSinkException {
        KeyedMessage message = new NamedVehicleMessage("foo");
        sink.register(ExactKeyMatcher.buildExactMatcher(message),
                listener);
        sink.receive(new VehicleMessage());
    }

    @Test
    public void receiveNonMatchingNotPropagated() throws DataSinkException {
        NamedVehicleMessage message = new NamedVehicleMessage("foo");
        sink.register(ExactKeyMatcher.buildExactMatcher(message), listener);
        message = new NamedVehicleMessage("bar");
        sink.receive(message);
        sink.clearQueue();
        assertThat(listener.received, nullValue());
    }

    @Test
    public void receiveUnrecognizedSimpleMessage() throws
            DataSinkException, UnrecognizedMeasurementTypeException {
        SimpleVehicleMessage message = new SimpleVehicleMessage("foo", "bar");
        sink.receive(message);
    }

    @Test
    public void receiveNonMatchingMeasurementNotPropagated() throws
            DataSinkException, UnrecognizedMeasurementTypeException {
        VehicleSpeed speed = new VehicleSpeed(42.0);
        sink.register(speed.getClass(), speedListener);
        SteeringWheelAngle angle = new SteeringWheelAngle(10.1);
        sink.receive(angle.toVehicleMessage());
        sink.clearQueue();
        assertThat(speedReceived, nullValue());
    }

    @Test
    public void listenerRecievesMessage() throws DataSinkException {
        NamedVehicleMessage message = new NamedVehicleMessage("foo");
        sink.register(ExactKeyMatcher.buildExactMatcher(message), listener);
        sink.receive(message);
        sink.clearQueue();
        assertThat(listener.received, notNullValue());
        assertEquals(listener.received, message);
    }

    @Test
    public void listenerReceivesMeasurement() throws DataSinkException,
           UnrecognizedMeasurementTypeException {
        VehicleSpeed speed = new VehicleSpeed(42.0);
        sink.register(speed.getClass(), speedListener);
        sink.receive(speed.toVehicleMessage());
        sink.clearQueue();
        assertThat(speedReceived, notNullValue());
        assertEquals(speedReceived, speed);
    }

    @Test
    public void removeMeasurementListenerByClass()
            throws UnrecognizedMeasurementTypeException, DataSinkException {
        VehicleSpeed speed = new VehicleSpeed(42.0);
        sink.register(speed.getClass(), speedListener);
        sink.unregister(speed.getClass(), speedListener);
        sink.receive(speed.toVehicleMessage());
        sink.clearQueue();
        assertThat(speedReceived, nullValue());
    }

    @Test
    public void removeMeasurementListener()
            throws UnrecognizedMeasurementTypeException, DataSinkException {
        VehicleSpeed speed = new VehicleSpeed(42.0);
        sink.register(speed.getClass(), speedListener);
        sink.unregister(
                BaseMeasurement.buildMatcherForMeasurement(speed.getClass()),
                speedListener);
        sink.receive(speed.toVehicleMessage());
        sink.clearQueue();
        assertThat(speedReceived, nullValue());
    }

    @Test
    public void removeMessageListener()
            throws UnrecognizedMeasurementTypeException, DataSinkException {
        NamedVehicleMessage message = new NamedVehicleMessage("foo");
        sink.register(ExactKeyMatcher.buildExactMatcher(message), listener);
        sink.unregister(ExactKeyMatcher.buildExactMatcher(message), listener);
        sink.receive(message);
        sink.clearQueue();
        assertThat(listener.received, nullValue());
    }

    @Test
    public void messageAndMeasurementListenersBothReceive() throws DataSinkException,
            UnrecognizedMeasurementTypeException {
        VehicleSpeed speed = new VehicleSpeed(42.0);
        VehicleMessage message = speed.toVehicleMessage();
        sink.register(ExactKeyMatcher.buildExactMatcher((KeyedMessage) message), listener);
        sink.register(speed.getClass(), speedListener);
        sink.receive(speed.toVehicleMessage());
        sink.clearQueue();
        assertThat(speedReceived, notNullValue());
        assertThat(listener.received, notNullValue());
    }

    @Test
    public void toStringNotNull() {
        assertThat(sink.toString(), notNullValue());
    }

    private VehicleSpeed.Listener speedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            speedReceived = (VehicleSpeed) measurement;
        }
    };

    private class SpyListener implements VehicleMessage.Listener {
        public VehicleMessage received;

        @Override
        public void receive(VehicleMessage message) {
            received = message;
        }
    };
}
