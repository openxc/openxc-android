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
import com.openxc.sources.TestSource;

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
        try {
            // Remove it so that the VehicleLocationProvider re-adds it with fresh
            // location history
            mLocationManager.removeTestProvider(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER);
        } catch(IllegalArgumentException e) {
        }

        try {
            // Remove it so that the VehicleLocationProvider re-adds it with fresh
            // location history
            mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch(IllegalArgumentException e) {
        }
    }

    // Due to bugs and or general crappiness in the ServiceTestCase, you will
    // run into many unexpected problems if you start the service in setUp - see
    // this blog post for more details:
    // http://convales.blogspot.de/2012/07/never-start-or-shutdown-service-in.html
    private void prepareServices() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), VehicleManager.class);
        manager = ((VehicleManager.VehicleBinder)
                bindService(startIntent)).getService();
        manager.waitUntilBound();
        manager.addSource(source);
        locationProvider = new VehicleLocationProvider(getContext(), manager);
        locationProvider.setOverwritingStatus(true);
    }

    @Override
    protected void tearDown() throws Exception {
        if(locationProvider != null) {
            locationProvider.stop();
        }
        super.tearDown();
    }

    @MediumTest
    public void testNoLocationMessages() {
        prepareServices();
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testNoLocationWithOnlyLatitude() {
        prepareServices();
        source.inject(Latitude.ID, latitude);
        TestUtils.pause(200);
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testNoLocationWithOnlyLongitude() {
        prepareServices();
        source.inject(Longitude.ID, longitude);
        TestUtils.pause(100);
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testNoLocationWithOnlySpeed() {
        prepareServices();
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(100);
        assertThat(mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER),
                nullValue());
    }

    @MediumTest
    public void testLocationWhenAllPresent() {
        prepareServices();
        source.inject(Latitude.ID, latitude);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(200);
        Location location = mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER);
        assertThat(location, notNullValue());
        assertThat(location.getLatitude(), equalTo(latitude));
        assertThat(location.getLongitude(), equalTo(longitude));
        assertThat(location.getSpeed(), equalTo(speed.floatValue()));
    }

    @MediumTest
    public void testOverwritesNativeGps() {
        prepareServices();
        source.inject(Latitude.ID, latitude);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(100);
        Location location = mLocationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER);
        assertThat(location, notNullValue());
        assertThat(location.getLatitude(), equalTo(latitude));
        assertThat(location.getLongitude(), equalTo(longitude));
        assertThat(location.getSpeed(), equalTo(speed.floatValue()));
    }

    @MediumTest
    public void testNotOverwrittenWhenDisabled() {
        prepareServices();
        locationProvider.setOverwritingStatus(false);
        source.inject(Latitude.ID, latitude);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(200);
        Location location = mLocationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER);
        assertThat(location, nullValue());
    }
}
