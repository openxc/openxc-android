package com.openxc.remote;

import com.openxc.remote.RawMeasurement;

/**
 * The interface for receiving a measurement update callback from the
 * VehicleService over AIDL.
 */
oneway interface VehicleServiceListenerInterface {
    void receive(String measurementId, in RawMeasurement value);
}
