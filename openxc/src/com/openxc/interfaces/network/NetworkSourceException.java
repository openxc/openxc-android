package com.openxc.interfaces.network;

import com.openxc.sources.DataSourceException;

public class NetworkSourceException extends DataSourceException {
    private static final long serialVersionUID = -6548438435453446857L;

    public NetworkSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkSourceException(String message) {
        super(message);
    }
}
