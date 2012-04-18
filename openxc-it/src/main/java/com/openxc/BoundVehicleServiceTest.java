package com.openxc;

import java.io.File;
import java.io.IOException;

import java.lang.InterruptedException;

import java.net.URISyntaxException;
import java.net.URI;

import org.apache.commons.io.FileUtils;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.remote.NoValueException;
import com.openxc.remote.RemoteVehicleServiceException;
import com.openxc.remote.RemoteVehicleService;
import com.openxc.remote.sources.trace.TraceVehicleDataSource;
import com.openxc.remote.sources.usb.UsbVehicleDataSource;

import com.openxc.VehicleService;

import android.content.Intent;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;

import junit.framework.Assert;

public class BoundVehicleServiceTest extends ServiceTestCase<VehicleService> {
    VehicleService service;
    VehicleSpeed speedReceived;
    SteeringWheelAngle steeringAngleReceived;
    URI traceUri;

    VehicleSpeed.Listener speedListener = new VehicleSpeed.Listener() {
        public void receive(VehicleMeasurement measurement) {
            speedReceived = (VehicleSpeed) measurement;
        }
    };

    SteeringWheelAngle.Listener steeringWheelListener =
            new SteeringWheelAngle.Listener() {
        public void receive(VehicleMeasurement measurement) {
            steeringAngleReceived = (SteeringWheelAngle) measurement;
        }
    };

    public BoundVehicleServiceTest() {
        super(VehicleService.class);
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
                    RemoteVehicleService.class));
        pause(200);
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleService.class);
        service = ((VehicleService.VehicleServiceBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();
        service.setDataSource(TraceVehicleDataSource.class.getName(),
                traceUri.toString());
    }

    private void checkReceivedMeasurement(VehicleMeasurement measurement) {
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
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        // let some measurements flow in so we have some known values
        pause(100);
        // kill the incoming data stream
        service.setDataSource(UsbVehicleDataSource.class.getName());
        pause(50);
        service.addListener(VehicleSpeed.class, speedListener);
        checkReceivedMeasurement(speedReceived);
    }

    @MediumTest
    public void testAddListener() throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        // let some measurements flow through the system
        pause(100);
        checkReceivedMeasurement(speedReceived);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements()
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        // let some measurements flow through the system
        pause(100);
        checkReceivedMeasurement(speedReceived);
        checkReceivedMeasurement(steeringAngleReceived);
    }

    @MediumTest
    public void testRemoveListener() throws RemoteVehicleServiceException,
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
            throws RemoteVehicleServiceException {
        service.removeListener(VehicleSpeed.class, speedListener);
        assertNull(speedReceived);
    }

    @MediumTest
    public void testRemoveOneMeasurementListener()
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        pause(100);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        pause(100);
        assertNull(speedReceived);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}

