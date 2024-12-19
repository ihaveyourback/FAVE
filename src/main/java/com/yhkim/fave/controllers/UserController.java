package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.EmailTokenEntity;
import com.yhkim.fave.entities.Report;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.repository.ReportRepository;
import com.yhkim.fave.results.CommonResult;
import com.yhkim.fave.results.Result;
import com.yhkim.fave.results.user.LoginResult;
import com.yhkim.fave.services.ReportService;
import com.yhkim.fave.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;

    @Autowired
    private ReportService reportService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/user/info")
    public String handleUserInfo(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);
            model.addAttribute("principal", principal); // Principal 객체도 추가
        } else {
            model.addAttribute("username", "Anonymous");
            model.addAttribute("principal", null);
        }
        return "userView"; // 공통 템플릿 이름
    }


    @PostMapping("/")
    @ResponseBody
    public String loginUser(@RequestParam String email, @RequestParam String password) {
        // 이메일과 비밀번호로 UserEntity 생성
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(password);

        // 서비스에서 사용자 인증 처리
        Result result = userService.login(user);
        System.out.println("123123" + result);

        // JSON 형태의 응답 생성
        JSONObject response = new JSONObject();
        if (result == CommonResult.SUCCESS) {
            response.put("result", "success");
            response.put("message", "로그인 성공!");
            // 필요한 추가 정보를 여기서 추가
        }
        return response.toString();
    }

    /**
     * 회원가입 요청 처리
     *
     * @param request HttpServletRequest 객체
     * @param user    UserEntity 객체
     * @return JSON 응답 문자열
     * @throws MessagingException 메시징 예외
     */
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(HttpServletRequest request, UserEntity user) throws MessagingException {
        Result result = this.userService.register(request, user);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }


    /**
     * 이메일 복구 요청 처리
     *
     * @param user UserEntity 객체
     * @return JSON 응답 문자열
     */
    @RequestMapping(value = "/recover-email", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getRecoverEmail(UserEntity user) {
        Result result = this.userService.recoverEmail(user);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        if (result == CommonResult.SUCCESS) {
            response.put("email", user.getEmail());
        }
        return response.toString();
    }

    /**
     * 비밀번호 복구 페이지 요청 처리
     *
     * @param userEmail 사용자 이메일 (옵션)
     * @param key       인증 키 (옵션)
     * @return ModelAndView 객체
     */
    @RequestMapping(value = "/recover-password", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRecoverPassword(@RequestParam(value = "userEmail", required = false) String userEmail,
                                           @RequestParam(value = "key", required = false) String key) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("userEmail", userEmail);
        modelAndView.addObject("key", key);
        modelAndView.setViewName("user/recoverPassword");
        return modelAndView;
    }

    /**
     * 비밀번호 재설정 요청 처리
     *
     * @param emailToken EmailTokenEntity 객체
     * @param password   새 비밀번호 (옵션)
     * @return JSON 응답 문자열
     */
    @RequestMapping(value = "/recover-password", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchRecoverPassword(EmailTokenEntity emailToken,
                                       @RequestParam(value = "password", required = false) String password) {
        Result result = this.userService.resolveRecoverPassword(emailToken, password);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }

    /**
     * 비밀번호 복구 요청 처리
     *
     * @param request HttpServletRequest 객체
     * @param email   사용자 이메일 (옵션)
     * @return JSON 응답 문자열
     * @throws MessagingException 메시징 예외
     */
    @RequestMapping(value = "/recover-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverPassword(HttpServletRequest request,
                                      @RequestParam(value = "email", required = false) String email) throws MessagingException {
        Result result = this.userService.provokeRecoverPassword(request, email);
        JSONObject response = new JSONObject();
        response.put(Result.NAME, result.nameToLower());
        return response.toString();
    }

    /**
     * 이메일 인증 토큰 검증 요청 처리
     *
     */
    @RequestMapping(value = "/validate-email-token", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getValidateEmailToken(@RequestParam("email") String email, @RequestParam("key") String key) {
        // 디버그 로그
        System.out.println("Received email: " + email);
        System.out.println("Received key: " + key);

        Result result = userService.validateEmailToken(email, key);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("result", result.nameToLower()); // 결과를 템플릿에 바인딩
        modelAndView.setViewName("user/validateEmailToken");
        return modelAndView;
    }

}
