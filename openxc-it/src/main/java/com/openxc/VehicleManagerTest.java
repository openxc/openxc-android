package com.openxc;

import com.openxc.VehicleManager;

import android.content.Intent;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class VehicleManagerTest extends ServiceTestCase<VehicleManager> {
    Intent startIntent;

    public VehicleManagerTest() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
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
        bindService(startIntent);
    }
}

