package com.openxc.remote;

import com.openxc.remote.RemoteVehicleService;

import com.openxc.remote.sources.ManualVehicleDataSource;

import android.content.Intent;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class RemoteVehicleServiceTest
        extends ServiceTestCase<RemoteVehicleService> {
    Intent startIntent;

    public RemoteVehicleServiceTest() {
        super(RemoteVehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startIntent = new Intent();
        startIntent.setClass(getContext(), RemoteVehicleServiceInterface.class);
    }

    @SmallTest
    public void testPreconditions() {
    }

    @SmallTest
    public void testStartable() {
        startService(startIntent);
    }

    @MediumTest
    public void testUsingUsbSource() {
        assertNotNull(bindService(startIntent));
    }

    @MediumTest
    public void testUsingManualSource() {
        startIntent.putExtra(RemoteVehicleService.DATA_SOURCE_NAME_EXTRA,
                ManualVehicleDataSource.class.getName());
        assertNotNull(bindService(startIntent));
    }
}
