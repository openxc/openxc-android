package com.openxc.sources;

public class DataSourceResourceException extends DataSourceException {
    private static final long serialVersionUID = -8121248199978505265L;

    public DataSourceResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceResourceException(String message) {
        super(message);
    }
}
