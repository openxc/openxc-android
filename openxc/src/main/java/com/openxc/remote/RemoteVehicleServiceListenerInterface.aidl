package com.openxc.remote;

import com.openxc.remote.RawMeasurement;
import com.openxc.remote.RawEventMeasurement;

oneway interface RemoteVehicleServiceListenerInterface {
    void receive(String measurementType, in RawMeasurement value);
}
