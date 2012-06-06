package com.openxc.controllers;

import com.openxc.measurements.MeasurementInterface;

import com.openxc.remote.RawMeasurement;

public interface VehicleController {
    public void set(String measurementId, RawMeasurement command);
}
