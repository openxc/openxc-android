package com.openxc;

import java.net.URI;

import junit.framework.Assert;
import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.messages.MessageKey;
import com.openxc.messages.NamedVehicleMessage;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleService;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.TestSource;

public class VehicleManagerTest extends ServiceTestCase<VehicleManager> {
    VehicleManager service;
    VehicleSpeed speedReceived;
    SteeringWheelAngle steeringAngleReceived;
    String receivedMessageId;
    TestSource source = new TestSource();
    VehicleMessage messageReceived;

    VehicleMessage.Listener messageListener = new VehicleMessage.Listener() {
        public void receive(VehicleMessage message) {
            messageReceived = message;
        }
    };

    VehicleSpeed.Listener speedListener = new VehicleSpeed.Listener() {
        public void receive(Measurement measurement) {
            speedReceived = (VehicleSpeed) measurement;
        }
    };

    SteeringWheelAngle.Listener steeringWheelListener =
            new SteeringWheelAngle.Listener() {
        public void receive(Measurement measurement) {
            steeringAngleReceived = (SteeringWheelAngle) measurement;
        }
    };

    public VehicleManagerTest() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        speedReceived = null;
        steeringAngleReceived = null;

        // if the service is already running (and thus may have old data
        // cached), kill it.
        getContext().stopService(new Intent(getContext(),
                    VehicleService.class));
    }

    // Due to bugs and or general crappiness in the ServiceTestCase, you will
    // run into many unexpected problems if you start the service in setUp - see
    // this blog post for more details:
    // http://convales.blogspot.de/2012/07/never-start-or-shutdown-service-in.html
    private void prepareServices() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        service = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();
        service.addSource(source);
    }

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
        super.tearDown();
    }

    @MediumTest
    public void testGetNoData() throws UnrecognizedMeasurementTypeException {
        prepareServices();
        try {
            service.get(EngineSpeed.class);
        } catch(NoValueException e) {
            return;
        }
        Assert.fail("Expected a NoValueException");
    }

    @MediumTest
    public void testListenForMessage() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        service.addListener(new NamedVehicleMessage("foo").getKey(),
                messageListener);
        source.inject("foo", 42.0);
        assertNotNull(messageReceived);
        assertEquals(messageReceived.asNamedMessage().getName(), "foo");
    }

    @MediumTest
    public void testListenForMeasurement() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        service.addListener(VehicleSpeed.class, speedListener);
        source.inject(VehicleSpeed.ID, 42.0);
        assertNotNull(speedReceived);
    }

    @MediumTest
    public void testCustomSink() throws DataSourceException {
        prepareServices();
        assertNull(receivedMessageId);
        service.addSink(mCustomSink);
        source.inject(VehicleSpeed.ID, 42.0);
        assertNotNull(receivedMessageId);
        service.removeSink(mCustomSink);
        receivedMessageId = null;
        source.inject(VehicleSpeed.ID, 42.0);
        assertNull(receivedMessageId);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        source.inject(VehicleSpeed.ID, 42.0);
        source.inject(SteeringWheelAngle.ID, 12.1);
        TestUtils.pause(5);
        assertNotNull(steeringAngleReceived);
        assertNotNull(speedReceived);
    }

    @MediumTest
    public void testRemoveMessageListener() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        MessageKey key = new NamedVehicleMessage("foo").getKey();
        service.addListener(key, messageListener);
        source.inject("foo", 42.0);
        messageReceived = null;
        service.removeListener(key, messageListener);
        source.inject("foo", 42.0);
        assertNull(messageReceived);
    }

    @MediumTest
    public void testRemoveMeasurementListener() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        service.addListener(VehicleSpeed.class, speedListener);
        source.inject(VehicleSpeed.ID, 42.0);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        source.inject(VehicleSpeed.ID, 42.0);
        TestUtils.pause(10);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testRemoveWithoutListening()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        service.removeListener(VehicleSpeed.class, speedListener);
    }

    @MediumTest
    public void testRemoveOneMeasurementListener()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        prepareServices();
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        source.inject(VehicleSpeed.ID, 42.0);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        source.inject(VehicleSpeed.ID, 42.0);
        TestUtils.pause(10);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testConsistentAge()
            throws UnrecognizedMeasurementTypeException,
            NoValueException, VehicleServiceException, DataSourceException {
        prepareServices();
        source.inject(VehicleSpeed.ID, 42.0);
        TestUtils.pause(1);
        Measurement measurement = service.get(VehicleSpeed.class);
        long age = measurement.getAge();
        assertTrue("Measurement age (" + age + ") should be > 5ms",
                age > 5);
    }

    @MediumTest
    public void testWrite() throws UnrecognizedMeasurementTypeException {
        prepareServices();
        service.send(new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT));
        // TODO how can we actually test that it gets written? might need to do
        // smaller unit tests.
    }

    @MediumTest
    public void testGetSpeed() throws UnrecognizedMeasurementTypeException,
            NoValueException {
        prepareServices();
        source.inject(VehicleSpeed.ID, 42.0);
        VehicleSpeed measurement = (VehicleSpeed)
                service.get(VehicleSpeed.class);
        assertNotNull(measurement);
        assertEquals(measurement.getValue().doubleValue(), 42.0);
    }

    // TODO get(Measurement)
    // TODO send(VehicleMessage)
    // TODO send(Measurement)
    // TODO add listener for measurement with class
    // TODO add listener for message with keyedmessage
    // TODO add listener for message with keymatcher
    // TODO add listener for message with message key
    // TODO remove measurement listener by class
    // TODO remove message listener by keyedmessage
    // TODO remove message listener by keymatcher
    // TODO remove message listener by messagekey
    // TODO add ousrce (already implicitly tested?)
    // TODO remove source
    // TODO add sink
    // TODO remove sink
    // TODO add vehicle interface by class
    // TODO remove vehicle interface by class
    // TODO set bluetooth polling
    // TODO get source summaries
    // TODO get sink summaries
    // TODO get active source types
    // TODO get message count
    // TODO get local vehicle interface
    // TODO remove local vehicle interface
    // TODO tostring

    private VehicleDataSink mCustomSink = new VehicleDataSink() {
        public void receive(VehicleMessage message) {
            receivedMessageId = ((NamedVehicleMessage)message).getName();
        }

        public void stop() { }
    };
}

