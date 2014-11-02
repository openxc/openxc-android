package com.openxc.sources;

public class DataSourceException extends Exception {
    private static final long serialVersionUID = -7086411602769827012L;

    public DataSourceException() { }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
