package com.openxc.sinks;

public class DataSinkException extends Exception {
    private static final long serialVersionUID = 245911625390405295L;

    public DataSinkException() { }

    public DataSinkException(String message) {
        super(message);
    }

    public DataSinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
