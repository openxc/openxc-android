package com.openxc.messages;

public class MismatchedMessageKeysException extends Exception {
    public MismatchedMessageKeysException(String message) {
        super(message);
    }

    public MismatchedMessageKeysException(String message,
            Throwable cause) {
        super(message, cause);
    }
}
