package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.*;
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
        // 게시글 페이징 정보 생성
        int totalPostCount = boardPostService.countPostsByUserEmail(principal.getName()); // 사용자의 게시물 수
        PageVo postPageVo = new PageVo(page, totalPostCount); // 게시글 페이지 정보 생성
        List<BoardPostEntity> posts = boardPostService.getPostsByUserEmail(principal.getName(), postPageVo); // 사용자의 게시물 목록 가져오기 (페이징 처리)

        // 신고 내역 페이징 정보 생성
        Pair<PageVo, List<ReportEntity>> reportPair = reportService.getReportsByLoggedInUser(reportPage, 10); // 사용자의 신고 목록 가져오기 (페이징 처리)
        PageVo reportPageVo = reportPair.getLeft();
        List<ReportEntity> reports = reportPair.getRight();

        // 찜 목록 가져오기
        List<FaveInfoEntity> favoritePosts = userService.getFavoritePostsByUserEmail(principal.getName()); // 사용자의 찜 목록 가져오기

        ModelAndView modelAndView = new ModelAndView(); // 뷰와 모델을 함께 설정 가능

        if (userDetails instanceof UserEntity user) {
            modelAndView.addObject("email", user.getEmail()); // 사용자 이메일
            modelAndView.addObject("nickname", user.getNickname()); // 사용자 닉네임
        }

        modelAndView.addObject("reports", reports);
        modelAndView.addObject("reportPageVo", reportPageVo);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("favoritePosts", favoritePosts); // 찜 목록 추가
        modelAndView.addObject("postPageVo", postPageVo);
        modelAndView.setViewName("user/profile");
        modelAndView.addObject("username", principal.getName()); // 사용자 이름
        return modelAndView;
    }

    // 회원탈퇴 메서드
    @PostMapping("/secession")
    public ResponseEntity<?> secession(@AuthenticationPrincipal Object principal, @RequestBody Map<String, String> payload) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "사용자 정보를 가져오는 데 실패했습니다."));
        }

        PrincipalDetails principalDetails;
        if (principal instanceof PrincipalDetails) {
            principalDetails = (PrincipalDetails) principal;
        } else if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User oauthUser = (CustomOAuth2User) principal;
            UserEntity user = new UserEntity();
            user.setEmail(oauthUser.getEmail());
            user.setNickname(oauthUser.getNickname());
            user.setOauth2Provider(oauthUser.getProvider());
            principalDetails = new PrincipalDetails(user, oauthUser.getAttributes());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "사용자 정보를 가져오는 데 실패했습니다."));
        }

        UserEntity user = principalDetails.getUser();
        if (!user.isSocialLogin()) {
            String currentPassword = payload.get("currentPassword");
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
            }
        }

        String email = user.getEmail();
        boolean isDeleted = userService.deactivateAccount(email);
        if (isDeleted) {
            return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요."));
        }
    }

    // 사용자 정보를 업데이트하는 메서드
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

        return ResponseEntity.ok(Map.of("message", "사용자 정보가 성공적으로 업데이트되었습니다. 로그아웃 후 변경된 비밀번호로 다시 로그인 해주세요."));
    }
}