package com.openxc;

import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;

import com.openxc.VehicleService;

import android.content.Intent;

import android.os.RemoteException;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class BoundVehicleServiceTest extends ServiceTestCase<VehicleService> {
    VehicleService service;

    public BoundVehicleServiceTest() {
        super(VehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleService.class);
        service = ((VehicleService.VehicleServiceBinder)
                bindService(startIntent)).getService();
    }

    @SmallTest
    public void testPreconditions() {
    }

    @MediumTest
    public void testGetUnbound() throws UnrecognizedMeasurementTypeException {
        service.unbindRemote();
        VehicleMeasurement measurement = service.get(VehicleSpeed.class);
        assertNotNull(measurement);
        assertFalse(measurement.hasValue());
    }

    @MediumTest
    public void testGetBound() throws UnrecognizedMeasurementTypeException {
        VehicleMeasurement measurement = service.get(VehicleSpeed.class);
        assertNotNull(measurement);
        assertFalse(measurement.hasValue());
    }

    @MediumTest
    public void testGetMocked() {
        assertTrue(false);
    }

    @MediumTest
    public void testAddListener() {
        assertTrue(false);
    }

    @MediumTest
    public void testAddListenerTwice() {
        assertTrue(false);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements() {
        assertTrue(false);
    }

    @MediumTest
    public void testRemoveListener() {
        assertTrue(false);
    }

    @MediumTest
    public void testRemoveTwoListeners() {
        assertTrue(false);
    }

    @MediumTest
    public void testRemoveWithoutListening() {
        assertTrue(false);
    }

    @MediumTest
    public void testRemoveOneMeasurementListener() {
        assertTrue(false);
    }
}

