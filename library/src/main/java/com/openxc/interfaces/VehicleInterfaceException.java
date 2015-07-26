package com.openxc.interfaces;

public class VehicleInterfaceException extends Exception {
    private static final long serialVersionUID = 6341748165315968366L;

    public VehicleInterfaceException() { }

    public VehicleInterfaceException(String message) {
        super(message);
    }

    public VehicleInterfaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
