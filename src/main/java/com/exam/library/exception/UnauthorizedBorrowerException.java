package com.exam.library.exception;

public class UnauthorizedBorrowerException extends RuntimeException {
    public UnauthorizedBorrowerException(String message) {
        super(message);
    }
}