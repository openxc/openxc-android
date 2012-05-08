package com.openxc;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceInterface;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.remote.sources.BaseVehicleDataSource;

import android.os.RemoteException;

import android.util.Log;

public class RemoteListenerSource extends BaseVehicleDataSource {
    private static String TAG = "RemoteListenerSource";

    private RemoteVehicleServiceInterface mService;

    public RemoteListenerSource(RemoteVehicleServiceInterface service) {
        super();
        mService = service;
    }

    private RemoteVehicleServiceListenerInterface mRemoteListener =
        new RemoteVehicleServiceListenerInterface.Stub() {
            public void receive(String measurementId, RawMeasurement value) {
                handleMessage(measurementId, value);
            }
        };

    public RemoteVehicleServiceListenerInterface getListener() {
        return mRemoteListener;
    }

    private void handleMessage(String name, RawMeasurement measurement) {
        try {
            mService.receive(name, measurement);
        } catch(RemoteException e) {
            Log.d(TAG, "Unable to send message to remote service", e);
        }
    }
}
