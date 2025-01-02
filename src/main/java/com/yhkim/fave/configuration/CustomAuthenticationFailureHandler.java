package com.yhkim.fave.configuration;

import com.yhkim.fave.exceptions.AccountDeletedException;
import com.yhkim.fave.exceptions.OAuth2IdNotFoundException;
import com.yhkim.fave.exceptions.UserNotVerifiedException;
import com.yhkim.fave.exceptions.UserSuspendedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String failureReason;
        int statusCode;

        if (exception instanceof OAuth2AuthenticationException) {
            if (exception.getCause() instanceof OAuth2IdNotFoundException) {
                failureReason = "oauth2IdNotFound";
                statusCode = 401;
            } else if (exception.getCause() instanceof OAuth2AuthenticationException) {
                failureReason = "oauth2_deleted"; // 탈퇴된 소셜 계정 메시지
                statusCode = 403;
            } else {
                failureReason = "oauth2_authentication_failed";
                statusCode = 401;
            }
        } else if (exception instanceof UserNotVerifiedException) {
            failureReason = "failure_not_verified";
            statusCode = 200;
        } else if (exception instanceof UserSuspendedException) {
            failureReason = "failure_suspended";
            statusCode = 403;
        } else if (exception instanceof AccountDeletedException) {
            failureReason = "failure_deleted"; // 일반 회원 탈퇴 메시지
            statusCode = 403;
        } else if (exception instanceof BadCredentialsException) {
            failureReason = "failure_bad_credentials";
            statusCode = 401;
        } else {
            failureReason = "failure_unknown";
            statusCode = 500;
        }

        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        response.getWriter().write("{\"error\": \"" + failureReason + "\"}");
    }
}