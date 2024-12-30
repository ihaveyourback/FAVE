// EmailAlreadyExistsException.java
package com.yhkim.fave.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}