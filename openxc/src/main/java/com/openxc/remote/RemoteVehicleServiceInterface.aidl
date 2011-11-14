package com.openxc.remote;

import com.openxc.remote.RemoteVehicleServiceListenerInterface;
import com.openxc.remote.RawStateMeasurement;
import com.openxc.remote.RawNumericalMeasurement;

interface RemoteVehicleServiceInterface {
    RawNumericalMeasurement getNumericalMeasurement(String measurementType);
    RawStateMeasurement getStateMeasurement(String measurementType);

    void addListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
    void removeListener(String measurementType,
            RemoteVehicleServiceListenerInterface listener);
}
