package com.openxc.measurements;

public class UnrecognizedMeasurementTypeException extends Exception {
    public UnrecognizedMeasurementTypeException(String message,
            Throwable cause) {
        super(message, cause);
    }
}
