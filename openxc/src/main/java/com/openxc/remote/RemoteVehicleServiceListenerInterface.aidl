package com.openxc.remote;

import com.openxc.remote.RawStateMeasurement;
import com.openxc.remote.RawNumericalMeasurement;

oneway interface RemoteVehicleServiceListenerInterface {
    void receiveNumerical(String measurementType,
            out RawNumericalMeasurement value);
    void receiveState(String measurementType, out RawStateMeasurement state);
}
