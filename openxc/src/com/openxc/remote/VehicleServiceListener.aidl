package com.openxc.remote;

import com.openxc.messages.VehicleMessage;

/**
 * The interface for receiving a measurement update callback from the
 * VehicleService over AIDL.
 */
oneway interface VehicleServiceListener {
    void receive(in VehicleMessage value);
}
