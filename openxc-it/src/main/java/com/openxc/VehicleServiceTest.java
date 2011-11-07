package com.openxc;

import com.openxc.VehicleService;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.SmallTest;

public class VehicleServiceTest extends ServiceTestCase<VehicleService> {
    public VehicleServiceTest() {
        super(VehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @SmallTest
    public void testPreconditions() {
        assertTrue(false);
    }
}
