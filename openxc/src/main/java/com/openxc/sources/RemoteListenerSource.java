package com.openxc.sources;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RemoteVehicleServiceListenerInterface;

import com.openxc.sources.BaseVehicleDataSource;

/**
 * TODO
 */
public class RemoteListenerSource extends BaseVehicleDataSource {
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
