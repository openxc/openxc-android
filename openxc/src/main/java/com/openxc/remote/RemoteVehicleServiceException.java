package com.openxc.remote;

public class RemoteVehicleServiceException extends Exception {
	private static final long serialVersionUID = -1889788234635180274L;

	public RemoteVehicleServiceException(String message, Throwable cause) {
        super(message, cause);
    }

	public RemoteVehicleServiceException(String message) {
        super(message);
    }
}
