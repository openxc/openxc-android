package com.openxc;

import java.lang.InterruptedException;

import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.NoValueException;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.WindshieldWiperSpeed;

import com.openxc.remote.RemoteVehicleService;

import com.openxc.remote.sources.trace.TraceVehicleDataSource;

import com.openxc.VehicleService;

import android.content.Intent;

import android.os.RemoteException;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;

public class MeasurementsTest extends ServiceTestCase<VehicleService> {
    VehicleService service;

    public MeasurementsTest() {
        super(VehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

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

    private void checkReceivedMeasurement(VehicleMeasurement measurement) {
        assertNotNull(measurement);
        assertFalse(measurement.isNone());
    }

    @MediumTest
    public void testGetSpeed() throws UnrecognizedMeasurementTypeException,
            NoValueException, RemoteException, InterruptedException {
        VehicleSpeed measurement = (VehicleSpeed)
                service.get(VehicleSpeed.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 42.0);
    }

    @MediumTest
    public void testGetWindshieldWiperSpeed()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        WindshieldWiperSpeed measurement = (WindshieldWiperSpeed)
                service.get(WindshieldWiperSpeed.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().doubleValue(), 11.0);
    }

    @MediumTest
    public void testGetBrakePedalStatus()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        BrakePedalStatus measurement = (BrakePedalStatus)
            service.get(BrakePedalStatus.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().booleanValue(), false);
    }

    @MediumTest
    public void testGetTransmissionGearPosition()
            throws UnrecognizedMeasurementTypeException, NoValueException,
            RemoteException, InterruptedException {
        pause(300);
        TransmissionGearPosition measurement = (TransmissionGearPosition)
                service.get(TransmissionGearPosition.class);
        checkReceivedMeasurement(measurement);
        assertEquals(measurement.getValue().enumValue(),
                TransmissionGearPosition.GearPosition.FIRST);
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch(InterruptedException e) {}
    }
}

