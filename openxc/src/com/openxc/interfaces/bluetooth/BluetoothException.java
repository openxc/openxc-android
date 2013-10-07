package com.openxc.interfaces.bluetooth;

public class BluetoothException extends Exception {
    private static final long serialVersionUID = 3052740555319473882L;

    public BluetoothException() { }

    public BluetoothException(String message) {
        super(message);
    }

    public BluetoothException(String message, Throwable cause) {
        super(message, cause);
    }
}
