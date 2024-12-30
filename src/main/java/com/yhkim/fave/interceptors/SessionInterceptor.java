package com.yhkim.fave.interceptors;

import com.yhkim.fave.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor { // HandlerInterceptor 인터페이스를 구현하는 세션 인터셉터 클래스
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청을 처리하기 전에 실행
        System.out.println("SessionInterceptor.preHandle 실행 됨!"); // 콘솔에 출력
        HttpSession session = request.getSession(); // 세션을 가져옴
        Object userObj = session.getAttribute("user"); // 세션에서 사용자 정보를 가져옴
        if (userObj == null || !(userObj instanceof UserEntity)) { // 로그인이 되어 있지 않은 경우 또는 사용자 정보가 아닌 경우
            response.setStatus(404); // 404 상태 코드를 설정
            return false;// false를 반환하여 요청을 중단
        }
        UserEntity user = (UserEntity) userObj;
//        if (!user.isAdmin()) {
//            response.setStatus(404);
//            return false;
//        }
        // 잠시 동안 /admin/** 접근을 허용하고 싶다면 아래 조건을 주석 처리 또는 변경
        if (!user.isAdmin()) {
            // response.setStatus(404); // 이 줄을 주석 처리하거나 다른 상태 코드를 설정
            return true;  // 관리자 권한이 없어도 /admin/** 접근 허용
        }
        return true;
    }

}
