package com.piiscan.database.exception;

public class DuplicateConnectionException extends RuntimeException {
    public DuplicateConnectionException(String message) {
        super(message);
    }
}
