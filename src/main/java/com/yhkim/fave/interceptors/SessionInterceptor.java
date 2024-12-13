package com.yhkim.fave.interceptors;

import com.yhkim.fave.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("SessionInterceptor.preHandle 실행 됨!");
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        if (userObj == null || !(userObj instanceof UserEntity)) {
            response.setStatus(404);
            return false;
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
