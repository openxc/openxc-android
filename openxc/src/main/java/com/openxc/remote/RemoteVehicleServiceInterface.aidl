package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;
import com.openxc.remote.RawEventMeasurement;

interface RemoteVehicleServiceInterface {
    RawEventMeasurement get(String measurementType);

    void addListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
    void removeListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
}
