package com.openxc.interfaces;

import com.openxc.sinks.VehicleDataSink;
import com.openxc.sources.VehicleDataSource;

public interface VehicleInterface extends VehicleDataSource, VehicleDataSink {

    /**
     * Determine if a given resource matches the on currently being used by the
     * VehicleInterface.
     *
     * This is useful to determine if you need to stop the currently running
     * instance of the interface and start it up again pointing to a different
     * resource.
     *
     * TODO this is stupid, the interface should have a setResource() method
     * instead that does whatever is neccsesary to restart the interface
     * internally.
     *
     * @return true if resource matches the current, active resource.
     */
    public boolean sameResource(String resource);
}
