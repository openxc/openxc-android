package com.openxc.remote.sources;

public class UsbDeviceException extends Exception {
	public UsbDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

	public UsbDeviceException(String message) {
        super(message);
    }
}
