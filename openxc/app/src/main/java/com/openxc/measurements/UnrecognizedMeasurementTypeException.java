package com.openxc.measurements;

public class UnrecognizedMeasurementTypeException extends Exception {
    private static final long serialVersionUID = 7166246234739984353L;

    public UnrecognizedMeasurementTypeException(String message) {
        super(message);
    }

    public UnrecognizedMeasurementTypeException(String message,
            Throwable cause) {
        super(message, cause);
    }
}
