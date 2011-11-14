package com.openxc.remote;

import com.openxc.remote.RawStateMeasurement;
import com.openxc.remote.RawNumericalMeasurement;

oneway interface RemoteVehicleServiceListenerInterface {
    void receiveNumerical(String measurementType,
            in RawNumericalMeasurement value);
    void receiveState(String measurementType, in RawStateMeasurement state);
}
