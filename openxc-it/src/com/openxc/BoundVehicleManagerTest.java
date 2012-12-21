package com.openxc;

import java.net.URI;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.TurnSignalStatus;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.NoValueException;
import com.openxc.remote.RawMeasurement;
import com.openxc.remote.VehicleServiceException;
import com.openxc.remote.VehicleService;

import com.openxc.sources.trace.TraceVehicleDataSource;
import com.openxc.sinks.BaseVehicleDataSink;
import com.openxc.sinks.VehicleDataSink;

import com.openxc.VehicleManager;

import android.content.Intent;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;

import junit.framework.Assert;

public class BoundVehicleManagerTest extends ServiceTestCase<VehicleManager> {
    VehicleManager service;
    VehicleSpeed speedReceived;
    SteeringWheelAngle steeringAngleReceived;
    URI traceUri;
    String receivedMeasurementId;
    TraceVehicleDataSource source;

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

    public BoundVehicleManagerTest() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        traceUri = TestUtils.copyToStorage(getContext(), R.raw.tracejson,
                "trace.json");

        speedReceived = null;
        steeringAngleReceived = null;

        // if the service is already running (and thus may have old data
        // cached), kill it.
        getContext().stopService(new Intent(getContext(),
                    VehicleService.class));
        TestUtils.pause(50);
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        service = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();
        source = new TraceVehicleDataSource(getContext(), traceUri);
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
        try {
            service.get(EngineSpeed.class);
        } catch(NoValueException e) {
            return;
        }
        Assert.fail("Expected a NoValueException");
    }

    @MediumTest
    public void testListenerGetsLastKnownValue()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        TestUtils.pause(100);
        // kill the incoming data stream
        service.removeSource(source);
        service.addListener(VehicleSpeed.class, speedListener);
        TestUtils.pause(20);
        assertNotNull(speedReceived);
    }

    @MediumTest
    public void testAddListener() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        // let some measurements flow through the system
        TestUtils.pause(100);
        assertNotNull(speedReceived);
    }

    @MediumTest
    public void testCustomSink() {
        assertNull(receivedMeasurementId);
        service.addSink(mCustomSink);
        TestUtils.pause(100);
        assertNotNull(receivedMeasurementId);
        service.removeSink(mCustomSink);
        receivedMeasurementId = null;
        TestUtils.pause(100);
        assertNull(receivedMeasurementId);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        // let some measurements flow through the system
        TestUtils.pause(100);
        assertNotNull(steeringAngleReceived);
        assertNotNull(speedReceived);
    }

    @MediumTest
    public void testRemoveListener() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        // let some measurements flow through the system
        TestUtils.pause(100);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        TestUtils.pause(100);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testRemoveWithoutListening()
            throws VehicleServiceException {
        service.removeListener(VehicleSpeed.class, speedListener);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testRemoveOneMeasurementListener()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        TestUtils.pause(100);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        TestUtils.pause(100);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testConsistentAge()
            throws UnrecognizedMeasurementTypeException,
            NoValueException, VehicleServiceException {
        TestUtils.pause(150);
        service.removeSource(source);
        TestUtils.pause(150);
        Measurement measurement = service.get(VehicleSpeed.class);
        double age = measurement.getAge();
        assertTrue("Measurement age (" + age + ") should be > 0.05",
                age > .05);
    }

    @MediumTest
    public void testWrite() throws UnrecognizedMeasurementTypeException {
        service.send(new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT));
        // TODO how can we actually test that it gets written? might need to do
        // smaller unit tests.
    }

    private VehicleDataSink mCustomSink = new BaseVehicleDataSink() {
        public boolean receive(RawMeasurement measurement) {
            receivedMeasurementId = measurement.getName();
            return true;
        }
    };
}

