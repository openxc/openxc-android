package com.openxc.sources;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.sources.BaseVehicleDataSource;

import android.util.Log;

public class RemoteListenerSource extends BaseVehicleDataSource {
    private static String TAG = "RemoteListenerSource";

    public RemoteListenerSource() {
        super();
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
