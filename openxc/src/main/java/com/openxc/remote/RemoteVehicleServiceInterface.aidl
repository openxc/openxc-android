package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;
import com.openxc.remote.RawMeasurement;

interface RemoteVehicleServiceInterface {
    RawMeasurement get(String measurementType);

    void addListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
    void removeListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
}
