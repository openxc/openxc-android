package com.openxc.sources;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceInterface;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.sources.BaseVehicleDataSource;

import android.os.RemoteException;

import android.util.Log;

/**
 * Pass measurements from a RemoteVehicleService to an in-process callback.
 *
 * This source is a bit of a special case - it's used by the
 * {@link com.openxc.VehicleService} to inject measurement updates from a
 * {@link com.openxc.remote.RemoteVehicleService} into an in-process data
 * pipeline. By using the same workflow as on the remote process side, we can
 * share code between remote and in-process data sources and sinks. This makes
 * adding new sources and sinks possible for end users, since the
 * RemoteVehicleService doesn't need to have every possible implementation.
 */
public class RemoteListenerSource extends BaseVehicleDataSource {
    private final static String TAG = "RemoteListenerSource";
    private RemoteVehicleServiceInterface mService;

    /**
     * Registers a measurement listener with the remote service.
     */
    public RemoteListenerSource(RemoteVehicleServiceInterface service) {
        super();
        mService = service;

        try {
            mService.register(mRemoteListener);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to register to receive " +
                    "measurement callbacks", e);
        }
    }

    public void stop() {
        super.stop();
        try {
            mService.unregister(mRemoteListener);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to register to receive " +
                    "measurement callbacks", e);
        }
    }

    private RemoteVehicleServiceListenerInterface mRemoteListener =
        new RemoteVehicleServiceListenerInterface.Stub() {
            public void receive(String measurementId,
                    RawMeasurement measurement) {
                handleMessage(measurementId, measurement.getValue(),
                        measurement.getEvent());
            }
        };
}
