package com.openxc.remote.sources;

public class VehicleDataSourceException extends Exception {
	private static final long serialVersionUID = -2874473990198685792L;

	public VehicleDataSourceException(String message) {
        super(message);
    }

    public VehicleDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
