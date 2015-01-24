package com.openxc.interfaces.usb;

import com.openxc.sources.DataSourceException;

public class UsbDeviceException extends DataSourceException {
    /**
     *
     */
    private static final long serialVersionUID = -7730917088324583224L;

    public UsbDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsbDeviceException(String message) {
        super(message);
    }
}
