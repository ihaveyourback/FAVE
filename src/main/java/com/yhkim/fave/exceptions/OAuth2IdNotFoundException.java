package com.yhkim.fave.exceptions;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2IdNotFoundException extends OAuth2AuthenticationException {
    public OAuth2IdNotFoundException(String message) {
        super(message);
    }
}