package com.example.biwooda.kakaoPay.exception;

public class NoUmbrellaLocker extends Exception {

    public NoUmbrellaLocker() {
        super("umbrella locker doesn't exist");
    }

    public NoUmbrellaLocker(String message, Throwable cause) {
        super(message, cause);
    }

    public NoUmbrellaLocker(Throwable cause) {
        super(cause);
    }
}
