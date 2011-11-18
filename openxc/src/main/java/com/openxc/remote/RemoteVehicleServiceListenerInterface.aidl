package com.openxc.remote;

import com.openxc.remote.RawMeasurement;

oneway interface RemoteVehicleServiceListenerInterface {
    void receive(String measurementType, in RawMeasurement value);
}
