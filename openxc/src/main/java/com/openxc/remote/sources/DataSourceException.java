package com.openxc.remote.sources;

public class DataSourceException extends Exception {
    public DataSourceException() { }

    public DataSourceException(String message, Exception e) {
        super(message, e);
    }
}
