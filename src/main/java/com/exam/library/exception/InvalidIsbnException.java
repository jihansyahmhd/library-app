package com.exam.library.exception;

public class InvalidIsbnException extends RuntimeException {
    public InvalidIsbnException(String message) {
        super(message);
    }
}