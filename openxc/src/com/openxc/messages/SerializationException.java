package com.openxc.messages;

public class SerializationException extends Exception {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message,
            Throwable cause) {
        super(message, cause);
    }
}
