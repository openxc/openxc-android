package com.openxc.messages;

public class VehicleMessageException extends Exception {
    private static final long serialVersionUID = 798026247659774953L;

    public VehicleMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public VehicleMessageException(String message) {
        super(message);
    }
}
