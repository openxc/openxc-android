package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;
import com.openxc.remote.RawStateMeasurement;
import com.openxc.remote.RawNumericalMeasurement;

interface RemoteVehicleServiceInterface {
    RawNumericalMeasurement getNumericalMeasurement(String measurementId);
    RawStateMeasurement getStateMeasurement(String measurementId);

    void addListener(RemoteVehicleServiceListenerInterface listener);
    void removeListener(RemoteVehicleServiceListenerInterface listener);
}
