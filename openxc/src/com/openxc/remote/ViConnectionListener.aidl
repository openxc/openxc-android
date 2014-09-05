package com.openxc.remote;

import com.openxc.interfaces.VehicleInterfaceDescriptor;

oneway interface ViConnectionListener {
    void onConnected(in VehicleInterfaceDescriptor vi);
    void onDisconnected();
}
