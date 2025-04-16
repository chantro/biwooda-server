package com.example.biwooda.kakaoPay.exception;

public class NoUmbrellaLeftException extends Exception {

    public NoUmbrellaLeftException() {
        super("no umbrella left");
    }

    public NoUmbrellaLeftException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoUmbrellaLeftException(Throwable cause) {
        super(cause);
    }
}
