package com.openxc.remote;

public class VehicleServiceException extends Exception {
    private static final long serialVersionUID = -1889788234635180274L;

    public VehicleServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public VehicleServiceException(String message) {
        super(message);
    }
}
