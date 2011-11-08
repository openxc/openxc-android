package com.openxc;

import com.openxc.measurements.VehicleMeasurement;
import com.openxc.measurements.VehicleSpeed;

import com.openxc.VehicleService;

import android.content.Intent;

import android.os.RemoteException;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

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
    public void testGetUnbound() {
        service.unbindRemote();
        try {
            service.get(VehicleSpeed.class);
            Assert.fail("should have thrown a RemoteException");
        } catch(RemoteException e) {
        }
    }

    @MediumTest
    public void testGetBound() throws RemoteException {
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

