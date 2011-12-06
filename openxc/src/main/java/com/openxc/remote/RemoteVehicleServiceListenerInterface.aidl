package com.openxc.remote;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RawMeasurement;

oneway interface RemoteVehicleServiceListenerInterface {
    void receive(String measurementId, in RawMeasurement value);
}
