package com.openxc.remote;

import com.openxc.remote.RawMeasurement;

/**
 * The interface for receiving a measurement update callback from the
 * VehicleService over AIDL.
 */
oneway interface VehicleServiceListener {
    void receive(in RawMeasurement value);
}
