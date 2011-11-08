package com.openxc.remote;

oneway interface RemoteVehicleServiceListenerInterface {
    void receiveNumerical(double value);
    void receiveState(String state);
}
