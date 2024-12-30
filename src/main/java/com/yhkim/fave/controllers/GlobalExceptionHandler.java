package com.yhkim.fave.controllers;

import com.yhkim.fave.exceptions.OAuth2IdNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleOAuth2IdNotFoundException(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage()); // Model에 추가
        System.out.println("에러에러에러에러에러에러에러에러에러" + ex.getMessage());
        return "home/index.main"; // 템플릿 반환
    }
}
