package com.openxc;

import java.lang.InterruptedException;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.SteeringWheelAngle;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.remote.RemoteVehicleServiceException;
import com.openxc.remote.RemoteVehicleService;

import com.openxc.remote.sources.trace.TraceVehicleDataSource;

import com.openxc.VehicleService;

import android.content.Intent;

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
        startIntent.putExtra(RemoteVehicleService.DATA_SOURCE_RESOURCE_EXTRA,
                "resource://" + R.raw.tracejson);
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
        assertFalse(measurement.isNone());
    }

    @MediumTest
    public void testGetNoData() throws UnrecognizedMeasurementTypeException {
        VehicleMeasurement measurement = service.get(EngineSpeed.class);
        assertNotNull(measurement);
        assertTrue(measurement.isNone());
    }

    @MediumTest
    public void testAddListener() throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        pause(400);
        checkReceivedMeasurement(speedReceived);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements()
            throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.addListener(SteeringWheelAngle.class, steeringWheelListener);
        pause(300);
        checkReceivedMeasurement(speedReceived);
        checkReceivedMeasurement(steeringAngleReceived);
    }

    @MediumTest
    public void testRemoveListener() throws RemoteVehicleServiceException,
            UnrecognizedMeasurementTypeException {
        service.addListener(VehicleSpeed.class, speedListener);
        service.removeListener(VehicleSpeed.class, speedListener);
        speedReceived = null;
        pause(300);
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
        pause(300);
        assertNull(speedReceived);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}

