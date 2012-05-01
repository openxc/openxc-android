package com.openxc.remote.sources;

public class DataSourceException extends Exception {
    public DataSourceException() { }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
