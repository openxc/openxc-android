package com.openxc.remote;

import com.openxc.remote.RawMeasurement;

/**
 * The interface for receiving a measurement update callback from the
 * RemoteVehicleService over AIDL.
 */
oneway interface RemoteVehicleServiceListenerInterface {
    void receive(String measurementId, in RawMeasurement value);
}
