package com.yhkim.fave.controllers;

import org.apache.catalina.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", 100); // 예제 값
        model.addAttribute("activeSessions", 5);
        model.addAttribute("errors", 0);
        return "admin/dashboard"; // admin/dashboard.html 템플릿 반환
    }

}