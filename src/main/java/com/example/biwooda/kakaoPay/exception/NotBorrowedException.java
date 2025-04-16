package com.example.biwooda.kakaoPay.exception;

public class NotBorrowedException extends Exception {

    public NotBorrowedException() {
        super("User didn't borrow");
    }

    public NotBorrowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotBorrowedException(Throwable cause) {
        super(cause);
    }
}
