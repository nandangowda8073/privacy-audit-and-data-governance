package com.piiscan.pii.exception;

public class PiiDetectionException extends RuntimeException {
    public PiiDetectionException(String message) { super(message); }
    public PiiDetectionException(String message, Throwable cause) { super(message, cause); }
}
