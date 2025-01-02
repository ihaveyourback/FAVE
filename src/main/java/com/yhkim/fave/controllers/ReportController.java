package com.yhkim.fave.controllers;

//import com.lsm.declaration.detail.CustomUserDetails;

import com.yhkim.fave.entities.ArticleEntity;
import com.yhkim.fave.entities.CommentEntity;
import com.yhkim.fave.entities.ReportEntity;
import com.yhkim.fave.reportrepository.BoardCommentRepository;
import com.yhkim.fave.reportrepository.BoardPostRepository;
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

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView CommentButton() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report/main");
        return modelAndView;
    }

    @RequestMapping(value = "/page", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView CommentReport(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "index", required = false) Integer index,
                                      @RequestParam(value = "commentIndex", required = false)Integer commentIndex){
        ModelAndView modelAndView = new ModelAndView();

        if(Objects.isNull(index) && Objects.isNull(commentIndex)){
            System.out.println("신고에 필요한 index가 누락됨. 둘 중 하나는 있어야함");
//            modelAndView.setViewName("report/main");
            // 오류 창으로 보내요
        }
//        if (userDetails instanceof CustomUserDetails user) {
//            modelAndView.addObject("email", user.getEmail());
//        }
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
            @AuthenticationPrincipal UserDetails user,
            ReportEntity report, CommentEntity comment,
            @RequestParam(value = "index", required = false) Integer index
    ) {
        if (index == null) {
            return ResponseEntity.badRequest().body("Index is required and cannot be null.");
        }

        try {
            report.setUserEmail(user.getUsername());
            // 신고하는 게시글 조회
            boolean suspended = reportService.checkIfSuspended();
            Result result = this.reportService.EmailDuplicate(report);
            if (suspended) {
                throw new IllegalStateException("계정이 정지된 사용자입니다.");
            }
            if ("신고 처리 완료".equals(report.getCurrentStatus())) {
                this.reportService.increaseWarningForReportedUser(report.getUserEmail());
            }
            JSONObject response = new JSONObject();
            response.put("result", result.name().toLowerCase());
            return ResponseEntity.ok(response.toString());
        } catch (IllegalStateException e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("result", "fail");
            errorResponse.put("message", e.getMessage()); // 상세 오류 메시지 포함
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