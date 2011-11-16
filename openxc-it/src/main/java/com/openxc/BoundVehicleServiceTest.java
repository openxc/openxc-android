package com.openxc;

import java.io.File;

import java.lang.InterruptedException;

import org.apache.commons.io.FileUtils;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.remote.RemoteVehicleServiceException;
import com.openxc.remote.RemoteVehicleService;

import com.openxc.remote.sources.TraceVehicleDataSource;

import com.openxc.VehicleService;

import android.content.Intent;

import android.os.RemoteException;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class BoundVehicleServiceTest extends ServiceTestCase<VehicleService> {
    VehicleService service;
    VehicleSpeed speedReceived;
    SteeringWheelAngle steeringAngleReceived;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        speedReceived = null;
        steeringAngleReceived = null;

        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleService.class);
        startIntent.putExtra(RemoteVehicleService.DATA_SOURCE_NAME_EXTRA,
                TraceVehicleDataSource.class.getName());
        FileUtils.copyInputStreamToFile(getContext().getResources().openRawResource(
                    R.raw.tracejson), new File("/data/data/com.openxc/trace.json"));
        startIntent.putExtra(RemoteVehicleService.DATA_SOURCE_RESOURCE_EXTRA,
                "file:///data/data/com.openxc/trace.json");
        service = ((VehicleService.VehicleServiceBinder)
                bindService(startIntent)).getService();
        // sleep for a moment to wait for the vehicle service to bind to the
        // remote service
        pause(200);
    }

    @SmallTest
    public void testPreconditions() {
    }

    private void checkReceivedMeasurement(VehicleMeasurement measurement) {
        assertNotNull(measurement);
        assertTrue(measurement.isNone());
    }

    @MediumTest
    public void testGetNoData() throws UnrecognizedMeasurementTypeException {
        VehicleMeasurement measurement = service.get(EngineSpeed.class);
        assertNotNull(measurement);
        assertFalse(measurement.isNone());
    }

    @MediumTest
    public void testGetMocked() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        VehicleSpeed measurement = (VehicleSpeed)
                service.get(VehicleSpeed.class);
        assertNotNull(measurement);
        assertTrue(measurement.isNone());
        assertEquals(measurement.getValue().doubleValue(), 42.0);
    }

    @MediumTest
    public void testAddListener() throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        pause(150);
        checkReceivedMeasurement(speedReceived);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements()
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        pause(150);
        checkReceivedMeasurement(speedReceived);
        checkReceivedMeasurement(steeringAngleReceived);
    }

    @MediumTest
    public void testRemoveListener() throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        pause(150);
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
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        pause(150);
        assertNull(speedReceived);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}

