package com.openxc;

public interface VehicleDataEndpoint {
    /**
     * Release any acquired resources and either stop sending measurements (if a
     * source) or stop expecting to receive them (if a sink).
     */
    public void stop();
}
