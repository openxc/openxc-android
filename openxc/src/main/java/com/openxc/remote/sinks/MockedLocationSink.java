package com.openxc.remote.sinks;

import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.VehicleSpeed;

import com.openxc.remote.RawMeasurement;

import android.content.Context;

import android.location.Location;
import android.location.LocationManager;

import android.util.Log;

public class MockedLocationSink extends ContextualVehicleDataSink {
    public final static String TAG = "MockedLocationSink";
    public final static String VEHICLE_LOCATION_PROVIDER = "vehicle";

    private LocationManager mLocationManager;

    public MockedLocationSink(Context context) {
        super(context);
        mLocationManager = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);
        setupMockLocations();
    }

    public void receive(String measurementId, RawMeasurement measurement) {
        if(measurementId.equals(Latitude.ID) ||
                measurementId.equals(Longitude.ID)) {
            updateLocation();
        }
    }

    private void updateLocation() {
        if(mMeasurements == null || mLocationManager == null ||
                !mMeasurements.containsKey(Latitude.ID) ||
                !mMeasurements.containsKey(Longitude.ID) ||
                !mMeasurements.containsKey(VehicleSpeed.ID)) {
            return;
                }

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(mMeasurements.get(Latitude.ID)
                .getValue().doubleValue());
        location.setLongitude(mMeasurements.get(Longitude.ID)
                .getValue().doubleValue());
        location.setSpeed(mMeasurements.get(VehicleSpeed.ID)
                .getValue().floatValue());
        location.setTime(System.currentTimeMillis());

        try {
            mLocationManager.setTestProviderLocation(
                    LocationManager.GPS_PROVIDER, location);
            location.setProvider(VEHICLE_LOCATION_PROVIDER);
            mLocationManager.setTestProviderLocation(
                    VEHICLE_LOCATION_PROVIDER, location);
        } catch(SecurityException e) {
            Log.w(TAG, "Unable to use mocked locations, " +
                    "insufficient privileges", e);
        }
    }

    /**
     * Setup Android location framework to accept vehicle GPS.
     *
     * If we have at least latitude, longitude and vehicle speed from
     * the vehicle, we send out a mocked location for the
     * LocationManager.GPS_PROVIDER and VEHICLE_LOCATION_PROVIDER
     * providers.
     *
     * Developers can either use the standard Android location framework
     * with mocked locations enabled, or the specific OpenXC
     * Latitude/Longitude measurements.
     */
    private void setupMockLocations() {
        try {
            mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                    false, false, false, false, false, true, false, 0, 5);
            mLocationManager.setTestProviderEnabled(
                    LocationManager.GPS_PROVIDER, true);

            if(mLocationManager.getProvider(
                        VEHICLE_LOCATION_PROVIDER) == null) {
                mLocationManager.addTestProvider(VEHICLE_LOCATION_PROVIDER,
                        false, false, false, false, false, true, false, 0, 5);
            }
            mLocationManager.setTestProviderEnabled(
                    VEHICLE_LOCATION_PROVIDER, true);
        } catch(SecurityException e) {
            Log.w(TAG, "Unable to use mocked locations, " +
                    "insufficient privileges", e);
            mLocationManager = null;
        }
    }
}
