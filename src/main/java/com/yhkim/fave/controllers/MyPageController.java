package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.entities.ReportEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.services.BoardPostService;
import com.yhkim.fave.services.ReportService;
import com.yhkim.fave.services.UserService;
import com.yhkim.fave.vos.PageVo;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/user")
public class MyPageController {
    private final UserService userService;
    private final ReportService reportService;
    private final BoardPostService boardPostService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MyPageController(UserService userService, ReportService reportService, BoardPostService boardPostService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.reportService = reportService;
        this.boardPostService = boardPostService;
        this.passwordEncoder = passwordEncoder;
    }

    // 프로필 페이지를 표시하는 메서드
    @GetMapping("/profile")
    public ModelAndView profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model, Principal principal,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "1") int reportPage) { // 페이지 번호 (기본값: 1)
        int totalCount = boardPostService.countPostsByUserEmail(principal.getName()); // 사용자의 게시물 수
        PageVo pageVo = new PageVo(page, totalCount); // 페이지 정보 생성
        List<BoardPostEntity> posts = boardPostService.getPostsByUserEmail(principal.getName(), pageVo); // 사용자의 게시물 목록 가져오기 (페이징 처리)
        Pair<PageVo, List<ReportEntity>> reportEntities = reportService.getReportsByLoggedInUser(reportPage, 10); // 사용자의 신고 목록 가져오기 (페이징 처리)

        ModelAndView modelAndView = new ModelAndView(); // 뷰와 모델을 함께 설정 가능

        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("email", user.getEmail()); // 사용자 이메일
            modelAndView.addObject("nickname", user.getNickname()); // 사용자 닉네임
        }

        modelAndView.addObject("reports", reportEntities.getRight()); // 신고 내역 리스트 추가
        modelAndView.addObject("reportPageVo", reportEntities.getLeft()); // 신고 페이징 정보 추가
        modelAndView.addObject("posts", posts); // 게시물 리스트 추가
        modelAndView.addObject("pageVo", pageVo); // 게시물 페이징 정보 추가
        modelAndView.setViewName("user/profile");
        modelAndView.addObject("username", principal.getName()); // 사용자 이름 추가
        return modelAndView;
    }


    // 회원탈퇴를 처리하는 메서드
    @PostMapping("/secession")
    public ResponseEntity<?> secession(@AuthenticationPrincipal UserDetails userDetails,// 사용자 정보
                                       @RequestBody Map<String, String> payload) {// 요청 본문
        if (userDetails instanceof UserEntity user) {// 사용자 정보가 있으면
            String email = user.getEmail(); // 사용자 이메일
            String currentPassword = payload.get("currentPassword");// 현재 비밀번호

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {// 현재 비밀번호가 일치하지 않으면
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
            }

            boolean isDeleted = userService.deactivateAccount(email);// 회원탈퇴 처리
            if (isDeleted) {// 회원탈퇴가 완료되면
                return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요."));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "사용자 정보를 가져오는 데 실패했습니다."));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateUserInfo(
            @AuthenticationPrincipal UserDetails userDetails, // 사용자 정보
            @RequestBody Map<String, String> payload, // 요청 본문
            HttpServletRequest request) { // 사용자 정보 업데이트

        if (!(userDetails instanceof UserEntity user)) { // 사용자 정보가 없으면
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "사용자 정보가 없습니다. 소셜 로그인은 사용불가능합니다."
            ));
        }

        String newNickname = payload.get("nickname"); // 새 닉네임
        String currentPassword = payload.get("currentPassword"); // 현재 비밀번호
        String newPassword = payload.get("newPassword"); // 새 비밀번호
//        System.out.println("닉네임:"+ newNickname);
//        System.out.println("지금비밀번호:"+ currentPassword);
//        System.out.println("새비밀번호:"+ newPassword);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
        }

        if (newNickname != null && !newNickname.isEmpty()) {
            if (!userService.updateNickname(user.getEmail(), newNickname)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "중복된 닉네임입니다."));
            }
        }

        if (newPassword != null && !newPassword.isEmpty()) { // 새 비밀번호가 있으면
            userService.updatePassword(user.getEmail(), newPassword); // 비밀번호 업데이트
        }

        // 세션 무효화 및 인증 정보 지우기
        request.getSession().invalidate(); // 세션 무효화
        SecurityContextHolder.clearContext(); // 인증 정보 지우기
        System.out.println("응답 메시지: " + Map.of("message", "사용자 정보가 성공적으로 업데이트되었습니다."));
        return ResponseEntity.ok(Map.of("message", "사용자 정보가 성공적으로 업데이트되었습니다. 로그아웃 후 변경된 비밀번호로 다시 로그인 해주세요."));
    }
}
