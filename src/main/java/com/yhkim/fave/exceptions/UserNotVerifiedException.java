package com.yhkim.fave.exceptions;

import org.springframework.security.core.AuthenticationException;
import com.yhkim.fave.entities.UserEntity;

public class UserNotVerifiedException extends AuthenticationException {
    private final UserEntity user;

    public UserNotVerifiedException(String msg, UserEntity user) {
        super(msg);
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }
}
