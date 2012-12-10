package com.openxc.sources.bluetooth;

public class BluetoothException extends Exception {
    public BluetoothException() { }

    public BluetoothException(String message) {
        super(message);
    }

	public BluetoothException(String message, Throwable cause) {
        super(message, cause);
    }
}
