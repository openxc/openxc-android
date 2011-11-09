package com.openxc.remote.sources;

public class VehicleDataSourceException extends Exception {
    public VehicleDataSourceException(String message) {
        super(message);
    }

    public VehicleDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
