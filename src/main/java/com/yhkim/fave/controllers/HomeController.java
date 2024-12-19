package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.UserEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/")
public class HomeController {

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getIndex(@AuthenticationPrincipal UserDetails userDetails) {
        ModelAndView modelAndView = new ModelAndView();
        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("now", LocalDateTime.now());
            modelAndView.addObject("isAdmin", user.isAdmin());
            modelAndView.addObject("nickname", user.getUsername());
            modelAndView.addObject("name", user.getNickname());
        }

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
    public String getLogout(HttpSession session){
     session.setAttribute("user", null);
     return "redirect:/";
    }

}
