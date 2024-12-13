package com.yhkim.fave.exceptions;

public class TransactionalException extends RuntimeException {
    public TransactionalException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionalException(String message) {
        super(message);
    }

    public TransactionalException() {
        super();
    }

    public TransactionalException(Throwable cause) {
        super(cause);
    }

}
