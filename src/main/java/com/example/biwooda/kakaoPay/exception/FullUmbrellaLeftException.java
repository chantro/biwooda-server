package com.example.biwooda.kakaoPay.exception;

public class FullUmbrellaLeftException extends Exception {

    public FullUmbrellaLeftException() {
        super("full umbrella left");
    }

    public FullUmbrellaLeftException(String message, Throwable cause) {
        super(message, cause);
    }

    public FullUmbrellaLeftException(Throwable cause) {
        super(cause);
    }
}
