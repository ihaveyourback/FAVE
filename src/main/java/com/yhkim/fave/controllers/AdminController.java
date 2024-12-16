package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;


@Controller
@RequestMapping("/admin")
public class AdminController {


    @GetMapping("/user")  //관리자 메인 페이지
    public ModelAndView  getAdminUserPage(@AuthenticationPrincipal UserDetails userDetails)  {
        ModelAndView modelAndView = new ModelAndView();
        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("now", LocalDateTime.now());
            modelAndView.addObject("isAdmin", user.isAdmin());
            modelAndView.addObject("nickname", user.getUsername());
        }
        modelAndView.setViewName("admin/adminIndex");
        return modelAndView;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", 100); // 예제 값
        model.addAttribute("activeSessions", 5);
        model.addAttribute("errors", 0);
        return "admin/dashboard"; // admin/dashboard.html 템플릿 반환
    }

}
