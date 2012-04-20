package com.openxc.remote;

import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sinks.DataSinkException;
import com.openxc.remote.sinks.DefaultDataSink;
import com.openxc.remote.sinks.FileRecorderSink;

import com.openxc.remote.sources.usb.UsbVehicleDataSource;

import android.app.Service;

import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;

import android.os.Looper;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import android.util.Log;

/**
 * The RemoteVehicleService is the centralized source of all vehicle data.
 *
 * To minimize overhead, only one object connects to the current vehicle data
 * source (e.g. a CAN translator or trace file being played back) and all
 * application requests are eventually propagated back to this service.
 *
 * Applications should not use this service directly, but should bind to the
 * in-process {@link com.openxc.VehicleService} instead - that has an interface
 * that respects Measurement types. The interface used for the
 * RemoteVehicleService is purposefully primative as there are a small set of
 * objects that can be natively marshalled through an AIDL interface.
 *
 * The service initializes and connects to the vehicle data source when bound.
 * The data source is selected by the application by passing extra data along
 * with the bind Intent - see the {@link #onBind(Intent)} method for details.
 * Only one data source is supported at a time.
 *
 * When a message is received from the data source, it is passed to any and all
 * registered message "sinks" - these receivers conform to the
 * {@link com.openxc.remote.sinks.VehicleDataSinkInterface}. There will always
 * be at least one sink that stores the latest messages and handles passing on
 * data to users of the VehicleService class. Other possible sinks include the
 * {@link com.openxc.remote.sinks.FileRecorderSink} which records a trace of the
 * raw OpenXC measurements to a file and a web streaming sink (which streams the
 * raw data to a web application). Users cannot register additional sinks at
 * this time, but the feature is planned.
 */
public class RemoteVehicleService extends Service {
    private final static String TAG = "RemoteVehicleService";
    private final static int NATIVE_GPS_UPDATE_INTERVAL = 5000;
    private final static String[] DEFAULT_DATA_SOURCES = {
            UsbVehicleDataSource.class.getName(),
    };
    private final static String[] DEFAULT_DATA_SINKS = {
            DefaultDataSink.class.getName(),
    };

    private NativeLocationListener mNativeLocationListener;
    private WakeLock mWakeLock;
    private DataPipeline mPipeline;
    private MeasurementNotifier mNotifier;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        mPipeline = new DataPipeline(this);
        initializeDefaultSinks();
        initializeDefaultSources();
        acquireWakeLock();
    }

    /**
     * Shut down any associated services when this service is about to die.
     *
     * This stops the data source (e.g. stops trace playback) and kills the
     * thread used for notifying measurement listeners.
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Service being destroyed");
        if(mPipeline != null) {
            mPipeline.stop();
        }
        releaseWakeLock();
    }

    /**
     * Initialize the service and data source when a client binds to us.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);

        mPipeline.removeSink(mNotifier);
        try {
            mNotifier = (MeasurementNotifier) mPipeline.addSink(
                    MeasurementNotifier.class.getName());
        } catch(DataSinkException e) {
            Log.w(TAG, "Unable to add notifier to pipeline sinks", e);
        }

        initializeDefaultSources();
        return mBinder;
    }

    /**
     * Reset the data source to the default when all clients disconnect.
     *
     * Since normal service users that want the default (i.e. USB device) don't
     * call setDataSource, they get stuck in a situation where a trace file
     * is being used.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        initializeDefaultSources();
        return false;
    }

    private void initializeDefaultSinks() {
        mPipeline.clearSinks();
        for(String sinkName : DEFAULT_DATA_SINKS) {
            try {
                mPipeline.addSink(sinkName);
            } catch(DataSinkException e) {
                Log.w(TAG, "Unable to add data sink " + sinkName, e);
            }
        }
    }

    private void initializeDefaultSources() {
        mPipeline.clearSources();
        for(String sourceName : DEFAULT_DATA_SOURCES) {
            mPipeline.addSource(sourceName);
        }
    }

    private final RemoteVehicleServiceInterface.Stub mBinder =
        new RemoteVehicleServiceInterface.Stub() {
            public RawMeasurement get(String measurementId)
                    throws RemoteException {
                return mPipeline.get(measurementId);
            }

            public void addListener(String measurementId,
                    RemoteVehicleServiceListenerInterface listener) {
                Log.i(TAG, "Adding listener " + listener + " to " +
                        measurementId);
                mNotifier.register(measurementId, listener);

                if(mPipeline.containsMeasurement(measurementId)) {
                    // send the last known value to the new listener
                    RawMeasurement rawMeasurement =
                        mPipeline.get(measurementId);
                    try {
                        listener.receive(measurementId, rawMeasurement);
                    } catch(RemoteException e) {
                        Log.w(TAG, "Couldn't notify application " +
                                "listener -- did it crash?", e);
                    }
                }
            }

            public void removeListener(String measurementId,
                    RemoteVehicleServiceListenerInterface listener) {
                Log.i(TAG, "Removing listener " + listener + " from " +
                        measurementId);
                mNotifier.unregister(measurementId, listener);
            }

            public void setDataSource(String dataSource, String resource) {
                Log.i(TAG, "Setting data source to " + dataSource +
                        " with resource " + resource);
                // TODO clearing everything when adding is a legacy feature
                mPipeline.clearSources();
                mPipeline.addSource(dataSource, resource);
            }

            public void enableRecording(boolean enabled) {
                Log.i(TAG, "Setting trace recording status to " + enabled);
                RemoteVehicleService.this.enableRecording(enabled);
            }

            public void enableNativeGpsPassthrough(boolean enabled) {
                Log.i(TAG, "Setting native GPS passtrough status to " +
                        enabled);
                RemoteVehicleService.this.enableNativeGpsPassthrough(enabled);
            }

            public int getMessageCount() {
                return RemoteVehicleService.this.getMessageCount();
            }
    };

    // TODO convert this to a data source
    private class NativeLocationListener extends Thread
            implements LocationListener {
        public void run() {
            Looper.myLooper().prepare();
            LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

            // try to grab a rough location from the network provider before
            // registering for GPS, which may take a while to initialize
            Location lastKnownLocation = locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
            if(lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            }

            try {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        NATIVE_GPS_UPDATE_INTERVAL, 0,
                        this);
                Log.d(TAG, "Requested GPS updates");
            } catch(IllegalArgumentException e) {
                Log.w(TAG, "GPS location provider is unavailable");
            }
            Looper.myLooper().loop();
        }

        public void onLocationChanged(final Location location) {
            mPipeline.receive(Latitude.ID, location.getLatitude(), null);
            mPipeline.receive(Longitude.ID, location.getLongitude(), null);
        }

        public void onStatusChanged(String provider, int status,
                Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };

    private void enableRecording(boolean enabled) {
        // TODO need a flag in this functio to say if it should be unique in its
        // type as we want to stop other recorders. for now we'll just clear 'em
        if(enabled) {
            initializeDefaultSinks();
            try {
                mPipeline.addSink(FileRecorderSink.class.getName());
            } catch(DataSinkException e) {
                Log.w(TAG, "Unable to enable recording", e);
            }
        } else {
             mPipeline.removeSink(FileRecorderSink.class.getName());
        }
    }

    private void enableNativeGpsPassthrough(boolean enabled) {
        if(enabled) {
            Log.i(TAG, "Enabled native GPS passthrough");
            mNativeLocationListener = new NativeLocationListener();
            mNativeLocationListener.start();
        } else if(mNativeLocationListener != null) {
            LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
            Log.i(TAG, "Disabled native GPS passthrough");
            locationManager.removeUpdates(mNativeLocationListener);
            mNativeLocationListener = null;
        }
    }

    private int getMessageCount() {
        return mPipeline.getMessageCount();
    }

    private void acquireWakeLock() {
        PowerManager manager = (PowerManager) getSystemService(
                Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
}
