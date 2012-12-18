package com.openxc.sources.ethernet;

import com.openxc.sources.DataSourceException;

public class EthernetDeviceException extends DataSourceException {
    private static final long serialVersionUID = -6548438435453446857L;

    public EthernetDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EthernetDeviceException(String message) {
        super(message);
    }
}
