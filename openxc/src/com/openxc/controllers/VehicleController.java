package com.openxc.controllers;

import com.openxc.remote.RawMeasurement;

public interface VehicleController {
    public boolean set(RawMeasurement command);
}
