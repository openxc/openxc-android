package com.openxc.sources;

import com.openxc.remote.RawMeasurement;

public class ApplicationSource extends BaseVehicleDataSource {
    public void handleMessage(String measurementId,
            RawMeasurement measurement) {
        handleMessage(measurementId, measurement.getValue(),
                measurement.getEvent());
    }
}
