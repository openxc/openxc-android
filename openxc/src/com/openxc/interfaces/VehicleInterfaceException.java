package com.openxc.interfaces;

public class VehicleInterfaceException extends Exception {
	public VehicleInterfaceException() { }

    public VehicleInterfaceException(String message) {
        super(message);
    }

    public VehicleInterfaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
