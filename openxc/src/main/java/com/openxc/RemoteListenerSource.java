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
            public void receive(String measurementId,
                    RawMeasurement measurement) {
                handleMessage(measurementId, measurement.getValue(),
                        measurement.getEvent());
            }
        };

    public RemoteVehicleServiceListenerInterface getListener() {
        return mRemoteListener;
    }
}
