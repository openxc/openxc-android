package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;

interface RemoteVehicleServiceInterface {
    double getNumericalMeasurement(String measurementId);
    String getStateMeasurement(String measurementId);

    void addListener(RemoteVehicleServiceListenerInterface listener);
    void removeListener(RemoteVehicleServiceListenerInterface listener);
}
