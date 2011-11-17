package com.openxc.remote.sources;

public class UsbDeviceException extends VehicleDataSourceException {
	public UsbDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

	public UsbDeviceException(String message) {
        super(message);
    }
}
