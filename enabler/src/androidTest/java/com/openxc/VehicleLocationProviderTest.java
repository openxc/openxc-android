package com.openxc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleService;
import com.openxc.remote.VehicleServiceException;
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
    }

    public static boolean mockLocationsEnabled(Context context) {
        return !Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
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
        try {
            manager.waitUntilBound();
        } catch(VehicleServiceException e) {
        }
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
    public void testNotOverwrittenWhenDisabled() {
        if(!mockLocationsEnabled(getContext())) {
            return;
        }

        prepareServices();
        locationProvider.setOverwritingStatus(false);
        source.inject(Latitude.ID, latitude + 1);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(100);
        Location lastAndroidLocation = mLocationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER);
        if(lastAndroidLocation != null) {
            assertTrue(lastAndroidLocation.getLatitude() != latitude + 1);
        }
    }

    @MediumTest
    public void testLocationWhenAllPresent() throws InterruptedException {
        if(!mockLocationsEnabled(getContext())) {
            return;
        }

        prepareServices();
        source.inject(Latitude.ID, latitude);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(1000);

        // LocationManager just does *not* seem to work on a 2.3.x emulator
        if(android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.HONEYCOMB) {
            Location lastVehicleLocation = mLocationManager.getLastKnownLocation(
                    VehicleLocationProvider.VEHICLE_LOCATION_PROVIDER);
            assertThat(lastVehicleLocation, notNullValue());
            assertThat(lastVehicleLocation.getLatitude(), equalTo(latitude));
            assertThat(lastVehicleLocation.getLongitude(), equalTo(longitude));
            assertThat(lastVehicleLocation.getSpeed(), equalTo(speed.floatValue()));
        }
    }

    @MediumTest
    public void testOverwritesNativeGps() throws InterruptedException {
        if(!mockLocationsEnabled(getContext())) {
            return;
        }

        prepareServices();
        source.inject(Latitude.ID, latitude);
        source.inject(Longitude.ID, longitude);
        source.inject(VehicleSpeed.ID, speed);
        TestUtils.pause(1000);

        // LocationManager just does *not* seem to work on a 2.3.x emulator
        if(android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.HONEYCOMB) {
            Location lastAndroidLocation = mLocationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
            assertThat(lastAndroidLocation, notNullValue());
            assertThat(lastAndroidLocation.getLatitude(), equalTo(latitude));
            assertThat(lastAndroidLocation.getLongitude(), equalTo(longitude));
            assertThat(lastAndroidLocation.getSpeed(), equalTo(speed.floatValue()));
        }
    }
}
