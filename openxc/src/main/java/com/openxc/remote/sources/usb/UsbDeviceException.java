package com.openxc.remote.sources.usb;

import com.openxc.remote.sources.VehicleDataSourceException;

public class UsbDeviceException extends VehicleDataSourceException {
	public UsbDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

	public UsbDeviceException(String message) {
        super(message);
    }
}
