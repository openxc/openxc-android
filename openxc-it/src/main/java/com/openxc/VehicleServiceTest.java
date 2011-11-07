package com.openxc;

import com.openxc.VehicleService;

import android.content.Intent;

import android.os.IBinder;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class VehicleServiceTest extends ServiceTestCase<VehicleService> {
    Intent startIntent;

    public VehicleServiceTest() {
        super(VehicleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleService.class);
    }

    @SmallTest
    public void testPreconditions() {
    }

    @SmallTest
    public void testStartable() {
        startService(startIntent);
    }

    @MediumTest
    public void testBindable() {
        IBinder service = bindService(startIntent);
    }
}
