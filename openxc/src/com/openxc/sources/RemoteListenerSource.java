package com.openxc.sources;

import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.MoreObjects;
import com.openxc.messages.VehicleMessage;
import com.openxc.remote.VehicleServiceInterface;
import com.openxc.remote.VehicleServiceListener;

/**
 * Pass messages from a VehicleService to an in-process callback.
 *
 * This source is a bit of a special case - it's used by the
 * {@link com.openxc.VehicleManager} to inject message updates from a
 * {@link com.openxc.remote.VehicleService} into an in-process data
 * pipeline. By using the same workflow as on the remote process side, we can
 * share code between remote and in-process data sources and sinks. This makes
 * adding new sources and sinks possible for end users, since the
 * VehicleService doesn't need to have every possible implementation.
 */
public class RemoteListenerSource extends BaseVehicleDataSource {
    private final static String TAG = "RemoteListenerSource";
    private VehicleServiceInterface mService;

    /**
     * Registers a message listener with the remote service.
     */
    public RemoteListenerSource(VehicleServiceInterface service) {
        mService = service;

        if(mService != null) {
            try {
                mService.register(mRemoteListener);
            } catch(RemoteException e) {
                Log.w(TAG, "Unable to register to receive " +
                        "message callbacks", e);
            }
        } else {
            Log.w(TAG, "Remote vehicle service is unexpectedly null");
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            mService.unregister(mRemoteListener);
        } catch(RemoteException e) {
            Log.w(TAG, "Unable to register to receive " +
                    "message callbacks", e);
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }

    private VehicleServiceListener mRemoteListener =
        new VehicleServiceListener.Stub() {
            @Override
            public void receive(VehicleMessage message) {
                handleMessage(message);
            }
        };
}
