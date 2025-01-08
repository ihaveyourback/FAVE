package com.yhkim.fave.configuration;

import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.exceptions.AccountDeletedException;
import com.yhkim.fave.exceptions.OAuth2IdNotFoundException;
import com.yhkim.fave.exceptions.UserNotVerifiedException;
import com.yhkim.fave.exceptions.UserSuspendedException;
import com.yhkim.fave.services.UserService;
import jakarta.mail.MessagingException;
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

    private final UserService userService;

    public CustomAuthenticationFailureHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String failureReason;
        int statusCode;

        if (exception instanceof UserNotVerifiedException) {
            failureReason = "failure_not_verified";
            statusCode = 200;
            UserEntity user = ((UserNotVerifiedException) exception).getUser();

            // 메일 재전송
            try {
                userService.handleUserNotVerified(user, null); // validationLink가 필요하다면 해당 부분 수정
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        } else if (exception instanceof UserSuspendedException) {
            failureReason = "failure_suspended";
            statusCode = 403;
        } else if (exception instanceof AccountDeletedException) {
            failureReason = "failure_deleted";
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
        response.getWriter().write("{\"result\": \"" + failureReason + "\"}");
    }
}
