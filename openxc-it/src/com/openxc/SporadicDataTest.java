package com.openxc;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.Measurement;

import com.openxc.remote.VehicleService;

import com.openxc.sources.trace.TraceVehicleDataSource;

import com.openxc.VehicleManager;


import android.content.Intent;

import android.test.ServiceTestCase;

import android.test.suitebuilder.annotation.MediumTest;

import junit.framework.Assert;

public class SporadicDataTest extends ServiceTestCase<VehicleManager> {
    VehicleManager service;
    int headlampStatusCount = 0;
    int parkingBrakeStatusCount = 0;
    URI traceUri;
    TraceVehicleDataSource source;

    HeadlampStatus.Listener headlampListener = new HeadlampStatus.Listener() {
        public void receive(Measurement measurement) {
            headlampStatusCount += 1;
        }
    };

    ParkingBrakeStatus.Listener parkingBrakeListener =
            new ParkingBrakeStatus.Listener() {
        public void receive(Measurement measurement) {
            parkingBrakeStatusCount += 1;
        }
    };

    public SporadicDataTest() {
        super(VehicleManager.class);
    }

    private URI copyToStorage(int resource, String filename) {
        URI uri = null;
        try {
            uri = new URI("file:///sdcard/com.openxc/" + filename);
        } catch(URISyntaxException e) {
            Assert.fail("Couldn't construct resource URIs: " + e);
        }

        try {
            FileUtils.copyInputStreamToFile(
                    getContext().getResources().openRawResource(resource),
                        new File(uri));
        } catch(IOException e) {
            Assert.fail("Couldn't copy trace files to SD card" + e);
        }
        return uri;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        traceUri = copyToStorage(R.raw.slowtrace, "slowtrace.json");

        // if the service is already running (and thus may have old data
        // cached), kill it.
        getContext().stopService(new Intent(getContext(), VehicleService.class));
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        service = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        service.waitUntilBound();

        service.addListener(HeadlampStatus.class, headlampListener);
        service.addListener(ParkingBrakeStatus.class, parkingBrakeListener);

        source = new TraceVehicleDataSource(getContext(), traceUri, false);
        service.addSource(source);
    }

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            source.stop();
        }
        super.tearDown();
    }

    @MediumTest
    public void testSlowSends() {
        TestUtils.pause(800);
        assertEquals(headlampStatusCount, 2);
        assertEquals(parkingBrakeStatusCount, 1);
    }
}

