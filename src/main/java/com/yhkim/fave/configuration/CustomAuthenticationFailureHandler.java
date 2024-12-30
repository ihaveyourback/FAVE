package com.yhkim.fave.configuration;

import com.yhkim.fave.exceptions.AccountDeletedException;
import com.yhkim.fave.exceptions.OAuth2IdNotFoundException;
import com.yhkim.fave.exceptions.UserNotVerifiedException;
import com.yhkim.fave.exceptions.UserSuspendedException;
import com.yhkim.fave.services.UserService;
import com.yhkim.fave.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        System.out.println(exception.toString());
        if (exception instanceof OAuth2AuthenticationException) { // OAuth2IdNotFoundException 예외가 발생한 경우
            System.out.println("!!?");
            request.getSession().setAttribute("errorMessage", exception.getMessage()); // 에러 메시지를 세션에 추가
            return;
        }

        response.setContentType("application/json"); // 응답 콘텐츠 타입 설정
        response.setCharacterEncoding("UTF-8"); // 응답 문자 인코딩 설정
        String failureReason;
        int statusCode;

        if (exception instanceof UserNotVerifiedException) {
            UserNotVerifiedException userException = (UserNotVerifiedException) exception;
            UserEntity user = userException.getUser();

            try {
                // 이메일 인증 링크를 생성하여 이메일로 전송
                String validationLink = "http://localhost:8080/user/validate-email-token?email=" + user.getEmail() + "&key=GENERATE_TOKEN_HERE"; // 실제 토큰 값으로 변경
                userService.handleUserNotVerified(user, validationLink); // 인증되지 않은 사용자에 대한 이메일 발송

                failureReason = "failure_not_verified"; // 이메일 전송 성공
                statusCode = 200; // 성공 상태 코드
            } catch (Exception e) {
                failureReason = "failure_email_send_failed"; // 이메일 전송 실패
                statusCode = 500; // 서버 오류 상태 코드
            }
        } else if (exception instanceof UserSuspendedException) {
            failureReason = "failure_suspended"; // 계정이 잠김
            statusCode = 403; // 금지 상태 코드
        } else if (exception instanceof AccountDeletedException) {
            failureReason = "failure_deleted"; // 계정이 삭제됨
            statusCode = 403; // 금지 상태 코드
        } else if (exception instanceof BadCredentialsException) {
            failureReason = "failure_bad_credentials"; // 아이디 또는 비밀번호가 일치하지 않음
            statusCode = 401; // 인증 실패 상태 코드
        } else {
            failureReason = exception.getMessage(); // 기타 예외 메시지 처리
            statusCode = 401; // 인증 실패 상태 코드
        }

        response.setStatus(statusCode); // 응답 상태 코드 설정
        response.getWriter().write("{\"result\": \"" + failureReason + "\"}"); // JSON 형식으로 실패 이유 작성
    }
}
