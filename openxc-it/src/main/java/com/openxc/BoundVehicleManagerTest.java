package com.openxc;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;

import java.net.URISyntaxException;
import java.net.URI;

import org.apache.commons.io.FileUtils;

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

    private void copyTraces() {
        try {
            traceUri = new URI("file:///sdcard/com.openxc/trace.json");
        } catch(URISyntaxException e) {
            Assert.fail("Couldn't construct resource URIs: " + e);
        }

        try {
            FileUtils.copyInputStreamToFile(
                    getContext().getResources().openRawResource(
                        R.raw.tracejson), new File(traceUri));
        } catch(IOException e) {}
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        copyTraces();

        speedReceived = null;
        steeringAngleReceived = null;

        // if the service is already running (and thus may have old data
        // cached), kill it.
        getContext().stopService(new Intent(getContext(),
                    VehicleService.class));
        pause(200);
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        service = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();
        service.addSource(new TraceVehicleDataSource(getContext(), traceUri));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(service != null)  {
            service.clearSources();
        }
    }

    private void checkReceivedMeasurement(Measurement measurement) {
        assertNotNull(measurement);
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
        pause(300);
        // kill the incoming data stream
        service.clearSources();
        pause(100);
        service.addListener(VehicleSpeed.class, speedListener);
        pause(50);
        checkReceivedMeasurement(speedReceived);
    }

    @MediumTest
    public void testAddListener() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        // let some measurements flow through the system
        pause(300);
        checkReceivedMeasurement(speedReceived);
    }

    @MediumTest
    public void testCustomSink() {
        assertNull(receivedMeasurementId);
        service.addSink(mCustomSink);
        pause(100);
        assertNotNull(receivedMeasurementId);
        service.removeSink(mCustomSink);
        receivedMeasurementId = null;
        pause(100);
        assertNull(receivedMeasurementId);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements()
            throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        // let some measurements flow through the system
        pause(100);
        checkReceivedMeasurement(speedReceived);
        checkReceivedMeasurement(steeringAngleReceived);
    }

    @MediumTest
    public void testRemoveListener() throws VehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        // let some measurements flow through the system
        pause(100);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        pause(100);
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
        pause(100);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        pause(100);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testConsistentAge()
            throws UnrecognizedMeasurementTypeException,
            NoValueException, VehicleServiceException {
        pause(150);
        service.clearSources();
        pause(150);
        Measurement measurement = service.get(VehicleSpeed.class);
        double age = measurement.getAge();
        assertTrue("Measurement age (" + age + ") should be > 0.05",
                age > .05);
    }

    @MediumTest
    public void testWrite() throws UnrecognizedMeasurementTypeException {
        service.set(new TurnSignalStatus(
                    TurnSignalStatus.TurnSignalPosition.LEFT));
        // TODO how can we actually test that it gets written? might need to do
        // smaller unit tests.
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }

    private VehicleDataSink mCustomSink = new BaseVehicleDataSink() {
        public void receive(RawMeasurement measurement) {
            receivedMeasurementId = measurement.getName();
        }
    };
}

