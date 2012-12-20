package com.openxc.interfaces;

import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.VehicleDataSource;

public interface VehicleInterface extends VehicleDataSource, VehicleDataSink {

    public boolean sameResource(String resource);
}
