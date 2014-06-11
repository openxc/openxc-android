package com.openxc.messages;

public class InvalidMessageFieldsException extends Exception {
    public InvalidMessageFieldsException(String message) {
        super(message);
    }

    public InvalidMessageFieldsException(String message,
            Throwable cause) {
        super(message, cause);
    }
}
