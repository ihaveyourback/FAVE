package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.*;
import com.yhkim.fave.services.BoardPostService;
import com.yhkim.fave.services.ReportService;
import com.yhkim.fave.services.UserService;
import com.yhkim.fave.vos.PageVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

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
    public ModelAndView profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model,                   Principal principal,
                                    @AuthenticationPrincipal Object principal2,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "1") int reportPage,
                                    @RequestParam(defaultValue = "1") int favoritePage) { // 페이지 번호 (기본값: 1)
        // 게시글 페이징 정보 생성
        int totalPostCount = boardPostService.countPostsByUserEmail(principal.getName()); // 사용자의 게시물 수
        PageVo postPageVo = new PageVo(page, totalPostCount); // 게시글 페이지 정보 생성
        List<BoardPostEntity> posts = boardPostService.getPostsByUserEmail(principal.getName(), postPageVo); // 사용자의 게시물 목록 가져오기 (페이징 처리)

        // 신고 내역 페이징 정보 생성
        Pair<PageVo, List<ReportEntity>> reportPair = reportService.getReportsByLoggedInUser(reportPage, 10); // 사용자의 신고 목록 가져오기
        PageVo reportPageVo = reportPair.getLeft();
        List<ReportEntity> reports = reportPair.getRight();

        // 찜 목록 페이징 정보 생성
        Pair<PageVo, List<FaveInfoEntity>> favoritePair = userService.getFavoritePostsByUserEmailWithPagination(principal.getName(), favoritePage, 5);
        PageVo favoritePageVo = favoritePair.getLeft();
        List<FaveInfoEntity> favoritePosts = favoritePair.getRight();

        ModelAndView modelAndView = new ModelAndView();// 뷰 객체 생성
        if (userDetails instanceof UserEntity user) {// 사용자 정보가 UserEntity 객체인 경우
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("isAdmin", user.isAdmin()); // 관리자 여부를 가져옴
            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("nickname", user.getNickname());

        }else if (principal2 instanceof CustomOAuth2User){ // 소셜 이메일 가져오기
            String email = ((CustomOAuth2User) principal2).getEmail();
            modelAndView.addObject("email", email);
        }
        // 기타 데이터 추가
        modelAndView.addObject("favoritePosts", favoritePosts);
        modelAndView.addObject("favoritePageVo", favoritePageVo);
        modelAndView.addObject("reports", reports);
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("reportPageVo", reportPageVo); // 신고 내역 페이지 정보 추가
        modelAndView.addObject("postPageVo", postPageVo); // 게시글 페이지 정보 추가

        modelAndView.addObject("username", principal.getName()); // 사용자 이름
        modelAndView.setViewName("user/profile");
        return modelAndView;
    }





    @PostMapping("/secession")
    public ResponseEntity<?> secession(@AuthenticationPrincipal Object principal,
                                       @RequestBody(required = false) Map<String, String> payload,
                                       HttpServletRequest request, HttpServletResponse response) {
        // 인증된 사용자인지 확인
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }

        // principal이 CustomOAuth2User인지, UserDetails인지 구분
        if (principal instanceof CustomOAuth2User) {
            // 소셜 로그인 처리
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;

            // 소셜 로그인 사용자의 이메일로 계정 처리
            boolean isDeleted = userService.deactivateAccount(customOAuth2User.getEmail());
            if (isDeleted) {
                // 소셜 로그인 세션 무효화
                SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
                logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

                return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요."));
            }
        } else if (principal instanceof UserDetails) {
            // 일반 로그인 처리
            UserDetails userDetails = (UserDetails) principal;

            // UserDetails에서 사용자 정보 추출
            UserEntity userEntity = extractUserEntity(userDetails);
            if (userEntity == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "사용자 정보를 찾을 수 없습니다."));
            }

            // 소셜 로그인 여부 확인 (isSocialLogin() 사용)
            boolean isSocialLogin = userEntity.isSocialLogin();

            if (isSocialLogin) {
                // 소셜 로그인 사용자는 비밀번호 확인 없이 탈퇴 가능
                boolean isDeleted = userService.deactivateAccount(userEntity.getEmail());
                if (isDeleted) {
                    // 소셜 로그인 세션 무효화
                    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
                    logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

                    return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요."));
                }
            } else {
                // 비밀번호 확인이 필요한 경우
                if (payload == null || !payload.containsKey("currentPassword")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("message", "현재 비밀번호를 입력해야 합니다."));
                }

                String currentPassword = payload.get("currentPassword");
                if (!passwordEncoder.matches(currentPassword, userEntity.getPassword())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "현재 비밀번호가 일치하지 않습니다."));
                }

                // 비밀번호가 맞다면 탈퇴 처리
                boolean isDeleted = userService.deactivateAccount(userEntity.getEmail());
                if (isDeleted) {
                    return ResponseEntity.ok(Map.of("message", "회원탈퇴가 완료되었습니다."));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요."));
                }
            }
        }

        // 위의 조건문을 빠져나왔다면 예외 상황
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "지원하지 않는 로그인 유형입니다."));
    }



    private UserEntity extractUserEntity(Object principal) {
        if (principal instanceof UserEntity) {
            return (UserEntity) principal;
        } else if (principal instanceof PrincipalDetails) {
            return ((PrincipalDetails) principal).getUser();
        } else if (principal instanceof UsernamePasswordAuthenticationToken) {
            Object authPrincipal = ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (authPrincipal instanceof UserEntity) {
                return (UserEntity) authPrincipal;
            } else if (authPrincipal instanceof PrincipalDetails) {
                return ((PrincipalDetails) authPrincipal).getUser();
            }
        }
        return null;
    }


    // 사용자 정보를 업데이트하는 메서드
    @PostMapping("/update-profile")
    public ResponseEntity<?> updateUserInfo(
            @AuthenticationPrincipal UserDetails userDetails, // 사용자 정보
            @RequestBody Map<String, String> payload, // 요청 본문
            HttpServletRequest request) { // 사용자 정보 업데이트
        if (!(userDetails instanceof UserEntity user)) { // 사용자 정보가 없으면
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "소셜 계정은 해당 소셜 페이지에서 수정 해야합니다."
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