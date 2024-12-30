package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.exceptions.EmailAlreadyExistsException;
import com.yhkim.fave.exceptions.OAuth2IdNotFoundException;
import com.yhkim.fave.services.OAuth2MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/")
public class HomeController {

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE) // HTML 반환
    public ModelAndView getIndex(@AuthenticationPrincipal UserDetails userDetails,
                                 HttpSession session) {// 사용자 정보를 가져오는 메서드
        ModelAndView modelAndView = new ModelAndView();// 뷰 객체 생성
        if (userDetails instanceof UserEntity user) {// 사용자 정보가 UserEntity 객체인 경우
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("isAdmin", user.isAdmin()); // 관리자 여부를 가져옴
        }

        System.out.println(session.getAttribute("errorMessage"));

        modelAndView.setViewName("home/index.main");
        return modelAndView;
    }



    // 로그인 성공 여부를 JSON으로 반환하는 API
    @RequestMapping(value = "/api/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody // JSON 반환
    public Map<String, String> login(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> response = new HashMap<>();
        if (userDetails != null) {
            response.put("result", "success");
        } else {
            response.put("result", "failure");
        }
        return response; // JSON 응답
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String getLogout(HttpSession session) {
        session.setAttribute("user", null);
        return "redirect:/";
    }
}