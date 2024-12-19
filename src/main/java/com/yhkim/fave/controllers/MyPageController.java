package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.Report;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.services.ReportService;
import com.yhkim.fave.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/user")
public class MyPageController {
    private final UserService userService;
    private final ReportService reportService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MyPageController(UserService userService , ReportService reportService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.reportService = reportService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public ModelAndView profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model, Principal principal) {
        // ReportService를 사용하여 로그인한 사용자의 신고 내역을 가져옴
        List<Report> reports = reportService.getReportsByLoggedInUser();
        ModelAndView modelAndView = new ModelAndView();

        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("email", user.getEmail());
        }

        modelAndView.addObject("reports", reports);
        modelAndView.setViewName("user/profile");
        modelAndView.addObject("username", principal.getName()); // 인증된 사용자 이름 추가
        return modelAndView; // Thymeleaf 템플릿 경로 반환 (예: profile.html)
    }

    // 회원탈퇴
    @PostMapping("/secession")
    public ResponseEntity<?> secession(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody Map<String, String> payload) {
        if (userDetails instanceof UserEntity user) {
            String email = user.getEmail();
            String currentPassword = payload.get("currentPassword");

            // 현재 비밀번호가 맞는지 확인 (예: 패스워드 매칭 로직 추가)
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
            }

            // 실제 탈퇴 처리 로직
            boolean isDeleted = userService.deactivateAccount(email);
            if (isDeleted) {
                return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요."));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "사용자 정보를 가져오는 데 실패했습니다."));
    }

}
