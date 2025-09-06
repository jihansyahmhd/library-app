package com.exam.library.exception;

public class BookNotBorrowedException extends RuntimeException {
    public BookNotBorrowedException(String message) {
        super(message);
    }
}