package com.openxc.remote;

oneway interface RemoteVehicleServiceListenerInterface {
    void receiveNumerical(String measurementType, double value);
    void receiveState(String measurementType, String state);
}
