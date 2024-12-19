package com.yhkim.fave.exceptions;

import org.springframework.security.core.AuthenticationException;

public class AccountDeletedException extends AuthenticationException {
    public AccountDeletedException(String message) {
        super(message);
    }
}
