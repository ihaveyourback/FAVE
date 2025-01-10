package com.yhkim.fave.controllers;

//import com.lsm.declaration.detail.CustomUserDetails;

import com.yhkim.fave.entities.*;
import com.yhkim.fave.repository.BoardCommentRepository;
import com.yhkim.fave.repository.BoardPostRepository;
import com.yhkim.fave.results.Result;
import com.yhkim.fave.services.ArticleService;
import com.yhkim.fave.services.CommentService;
import com.yhkim.fave.services.ReportService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping(value = "/report")
public class ReportController {
    private final ArticleService articleService;
    private final ReportService reportService;
    private final BoardCommentRepository boardCommentRepository;
    private final BoardPostRepository boardPostRepository;
    private final CommentService commentService;


    @Autowired
    public ReportController(ReportService reportService, ArticleService articleService, BoardCommentRepository boardCommentRepository, CommentService commentService, BoardPostRepository boardPostRepository) {

        this.reportService = reportService;
        this.articleService = articleService;
        this.boardCommentRepository = boardCommentRepository;
        this.commentService = commentService;
        this.boardPostRepository = boardPostRepository;
    }

//    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
//    public ModelAndView CommentButton() {
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("report/main");
//        return modelAndView;
//    }

    @RequestMapping(value = "/page", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getReport(@RequestParam(value = "index", required = false) Integer index,
                                  @RequestParam(value = "commentIndex", required = false) Integer commentIndex,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  @AuthenticationPrincipal Object principal) {

        ModelAndView modelAndView = new ModelAndView();
        String userEmail = null;
        if (userDetails instanceof UserEntity user) {
            userEmail = user.getEmail();
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("now", LocalDateTime.now());
            modelAndView.addObject("isAdmin", user.isAdmin());
            modelAndView.addObject("nickname", user.getNickname());
            modelAndView.addObject("email", userEmail);
//            System.out.println("나오나요:"+user.isAdmin());
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            userEmail = customOAuth2User.getEmail();
            modelAndView.addObject("email", userEmail); // 소셜 로그인 이메일 추가
            modelAndView.addObject("nickname", customOAuth2User.getNickname());
        }
// 로그인되지 않은 경우 이메일을 null로 전달
        modelAndView.addObject("email", userEmail);
        ArticleEntity article = articleService.getArticleByIndex(index);
        List<BoardCommentEntity> comments = boardCommentRepository.findByCommentIndex(commentIndex);
//        CommentEntity[] comments= commentService.getCommentsByPostId(article.getIndex());
        modelAndView.addObject("article", article);
        modelAndView.addObject("comments", comments);
        modelAndView.setViewName("report/report");
        return modelAndView;
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> createReport(
            ReportEntity report, CommentEntity comment,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "index", required = false) Integer index,
            @AuthenticationPrincipal Object principal
    ) {
        String currentUserEmail = null;

        // `principal`의 타입에 따라 처리
        if (principal instanceof UserDetails) {
            // 일반 로그인 사용자 처리
            UserDetails userDetails = (UserDetails) principal;
            currentUserEmail = userDetails.getUsername();
        } else if (principal instanceof CustomOAuth2User) {
            // 소셜 로그인 사용자 처리
            CustomOAuth2User oauthUser = (CustomOAuth2User) principal;
            currentUserEmail = oauthUser.getName(); // 이메일 또는 ID로 사용자 정보 반환
        } else {
            // 인증되지 않은 사용자 예외 처리
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("result", "fail");
            errorResponse.put("message", "사용자가 인증되지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse.toString());
        }

        try {
            // 현재 사용자 이메일 설정
            report.setUserEmail(currentUserEmail);

            // 신고하는 게시글 조회
            boolean suspended = reportService.checkIfSuspended(currentUserEmail);
            if (suspended) {
                throw new IllegalStateException("계정이 정지된 사용자입니다.");
            }

            // 신고 처리
            Result result = this.reportService.EmailDuplicate(report, principal);
            if ("신고 처리 완료".equals(report.getCurrentStatus())) {
                this.reportService.increaseWarningForReportedUser(report.getUserEmail());
            }

            // 성공 응답
            JSONObject response = new JSONObject();
            response.put("result", result.name().toLowerCase());
            return ResponseEntity.ok(response.toString());
        } catch (IllegalStateException e) {
            // 오류 응답
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("result", "fail");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse.toString());
        }
    }


    @RequestMapping(value = "/result", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView ReportResult() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report/result");
        return modelAndView;
    }
}