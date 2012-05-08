package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sinks.DefaultDataSink;
import com.openxc.remote.sinks.MockedLocationSink;
import com.openxc.remote.sinks.FileRecorderSink;
import com.openxc.remote.sinks.MeasurementNotifierSink;
import com.openxc.remote.sinks.VehicleDataSink;

import com.openxc.remote.sources.SourceCallback;
import com.openxc.remote.sources.DataSourceException;
import com.openxc.remote.sources.NativeLocationSource;
import com.openxc.remote.sources.usb.UsbVehicleDataSource;
import com.openxc.remote.sources.VehicleDataSource;

import com.openxc.util.AndroidFileOpener;

import android.app.Service;

import android.content.Context;
import android.content.Intent;

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

    private WakeLock mWakeLock;
    private DataPipeline mPipeline;
    private MeasurementNotifierSink mNotifier;
    private VehicleDataSource mNativeLocationSource;
    private VehicleDataSink mFileRecorder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");
        mPipeline = new DataPipeline();
        initializeDefaultSources();
        initializeDefaultSinks();
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
        mNotifier = new MeasurementNotifierSink();
        mPipeline.addSink(mNotifier);

        initializeDefaultSources();
        return mBinder;
    }

    /**
     * Reset the data source to the default when all clients disconnect.
     *
     * Since normal service users that want the default (i.e. USB device) don't
     * usually set a new data source, they get could stuck in a situation where
     * a trace file is being used if we don't reset it.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        initializeDefaultSources();
        return false;
    }

    private void initializeDefaultSinks() {
        mPipeline.addSink(new MockedLocationSink(this));
        mPipeline.addSink(new DefaultDataSink());
    }

    private void initializeDefaultSources() {
        mPipeline.clearSources();
        try {
            mPipeline.addSource(new UsbVehicleDataSource(this));
        } catch(DataSourceException e) {
            Log.w(TAG, "Unable to add default USB data source", e);
        }
    }

    private URI uriFromResourceString(String resource) {
        URI resourceUri = null;
        if(resource != null) {
            try {
                resourceUri = new URI(resource);
            } catch(URISyntaxException e) {
                Log.w(TAG, "Unable to parse resource as URI " + resource);
            }
        }
        return resourceUri;
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
            }

            public void removeListener(String measurementId,
                    RemoteVehicleServiceListenerInterface listener) {
                Log.i(TAG, "Removing listener " + listener + " from " +
                        measurementId);
                mNotifier.unregister(measurementId, listener);
            }

            public void resetDataSources() {
                initializeDefaultSources();
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

    private void enableRecording(boolean enabled) {
        if(enabled) {
            mFileRecorder = mPipeline.addSink(
                    new FileRecorderSink(new AndroidFileOpener(this)));
        } else if(mFileRecorder != null) {
             mPipeline.removeSink(mFileRecorder);
        }
    }

    private void enableNativeGpsPassthrough(boolean enabled) {
        if(enabled) {
            mNativeLocationSource = mPipeline.addSource(
                    new NativeLocationSource(this));
        } else if(mNativeLocationSource != null) {
            mPipeline.removeSource(mNativeLocationSource);
            mNativeLocationSource = null;
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
