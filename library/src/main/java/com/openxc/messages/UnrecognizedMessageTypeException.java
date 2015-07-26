package com.openxc.messages;

public class UnrecognizedMessageTypeException extends Exception {
    public UnrecognizedMessageTypeException(String message) {
        super(message);
    }

    public UnrecognizedMessageTypeException(String message,
            Throwable cause) {
        super(message, cause);
    }
}
