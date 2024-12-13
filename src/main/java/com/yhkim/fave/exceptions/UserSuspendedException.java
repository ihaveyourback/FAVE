package com.yhkim.fave.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UserSuspendedException extends AuthenticationException {
    public UserSuspendedException(String msg) {
        super(msg);
    }
}
