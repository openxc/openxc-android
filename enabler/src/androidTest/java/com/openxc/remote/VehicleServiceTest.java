package com.openxc.remote;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;

public class VehicleServiceTest extends ServiceTestCase<VehicleService> {
    Intent startIntent;

    public VehicleServiceTest() {
        super(VehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleServiceInterface.class);
        VehicleService.sIsUnderTest = true;
    }

    @MediumTest
    public void testUsingUsbSource() {
        assertNotNull(bindService(startIntent));
    }
}
