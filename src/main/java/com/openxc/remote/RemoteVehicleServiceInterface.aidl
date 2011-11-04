package com.openxc.remote;

interface RemoteVehicleServiceInterface {
    double getNumericalMeasurement(String measurementId);
    String getStateMeasurement(String measurementId);
}
