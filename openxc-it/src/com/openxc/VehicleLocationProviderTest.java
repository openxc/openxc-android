package com.openxc;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.messages.SimpleVehicleMessage;
import com.openxc.remote.VehicleService;
import com.openxc.VehicleManager;
import com.openxc.sources.BaseVehicleDataSource;
import com.openxc.sources.SourceCallback;

public class VehicleLocationProviderTest
        extends ServiceTestCase<VehicleManager> {
    VehicleManager manager;
    VehicleLocationProvider locationProvider;
    TestSource source;
    LocationManager mLocationManager;
    Double latitude = 42.1;
    Double longitude = 100.1;
    Double speed = 23.2;

    public VehicleLocationProviderTest() {
        super(VehicleManager.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = new TestSource();
        // if the service is already running (and thus may have old data
        // cached), kill it.
        getContext().stopService(new Intent(getContext(),
                    VehicleService.class));
        mLocationManager = (LocationManager) getContext().getSystemService(
                    Context.LOCATION_SERVICE);
        TestUtils.pause(50);
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        manager = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        manager.waitUntilBound();
        locationProvider = new VehicleLocationProvider(getContext(), manager);
    }

    @Override
    protected void tearDown() throws Exception {
        if(source != null) {
            locationProvider.stop();
        }
        super.tearDown();
    }

    @MediumTest
    public void testNoLocationMessages() {
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testNoLocationWithOnlyLatitude() {
        source.inject(Latitude.ID, latitude);
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testNoLocationWithOnlyLongitude() {
        source.inject(Longitude.ID, longitude);
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testNoLocationWithOnlySpeed() {
        source.inject(VehicleSpeed.ID, speed);
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testLocationWhenAllPresent() {
        source.inject(Latitude.ID, latitude);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        Location location = mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER);
        assertThat(location, notNullValue());
        assertThat(location.getLatitude(), equalTo(latitude));
        assertThat(location.getLongitude(), equalTo(longitude));
        assertThat(location.getSpeed(), equalTo(speed.floatValue()));
    }

    private class TestSource extends BaseVehicleDataSource {
        private SourceCallback callback;

        public void inject(String name, Object value) {
            if(callback != null) {
                callback.receive(new SimpleVehicleMessage(name, value));
            }
        }

        public void setCallback(SourceCallback theCallback) {
            callback = theCallback;
        }

        public void stop() {
            callback = null;
        }
    }
}
