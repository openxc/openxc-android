package com.openxc;

import com.openxc.VehicleService;

import android.content.Intent;

import android.os.IBinder;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class VehicleServiceTest extends ServiceTestCase<VehicleService> {
    IBinder service;

    public VehicleServiceTest() {
        super(VehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleService.class);
        service = bindService(startIntent);
    }

    @SmallTest
    public void testPreconditions() {
    }

    @MediumTest(expected=RemoteException)
    public void testGetUnbound() throws RemoteException {
        service.unbindRemote();
        VehicleMeasurement measurement = service.get(VehicleSpeed.class);
    }

    @MediumTest
    public void testGetBound() {
        VehicleMeasurement measurement = service.get(VehicleSpeed.class);
        assertFalse(measurement.hasValue());
    }

    @MediumTest
    public void testGetMocked() {
        assert(false);
    }

    @MediumTest
    public void testAddListener() {
        assert(false);
    }

    @MediumTest
    public void testAddListenerTwice() {
        assert(false);
    }

    @MediumTest
    public void testAddListenersTwoMeasurements() {
        assert(false);
    }

    @MediumTest
    public void testRemoveListener() {
        assert(false);
    }

    @MediumTest
    public void testRemoveTwoListeners() {
        assert(false);
    }

    @MediumTest
    public void testRemoveWithoutListening() {
        assert(false);
    }

    @MediumTest
    public void testRemoveOneMeasurementListener() {
        assert(false);
    }
}

