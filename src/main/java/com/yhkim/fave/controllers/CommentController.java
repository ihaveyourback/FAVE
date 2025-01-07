package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.CommentEntity;
import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.results.article.ArticleResult;
import com.yhkim.fave.results.comment.DeleteCommentResult;
import com.yhkim.fave.results.comment.ModifyCommentResult;
import com.yhkim.fave.services.CommentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/comment")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    //    // 댓글 작성 기능 (창윤)
//    @RequestMapping(value = "/write", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public String postIndex(CommentEntity comment) {
//        ArticleResult result = this.commentService.writeComment(comment);
//        JSONObject response = new JSONObject();
//        response.put("result", result);
//        return response.toString();
//    }

//댓글 작성 기능 (용현)
    @RequestMapping(value = "/write", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(CommentEntity comment, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "{\"result\":\"FAILURE\"}"; // 인증되지 않은 사용자 처리
        }
        // 인증된 사용자 정보 가져오기
        ArticleResult result = this.commentService.writeComment(comment, authentication);
        // 결과 반환
        JSONObject response = new JSONObject();
        response.put("result", result);
        return response.toString();
    }


//    // 대댓글 작성 엔드포인트
//    @PostMapping("/reply")
//    public ResponseEntity<String> replyComment(@RequestParam int parentCommentId, @RequestParam String content) {
//        ArticleResult result = commentService.saveReplyComment(parentCommentId, content);
//        if (result == ArticleResult.SUCCESS) {
//            return ResponseEntity.ok("Reply comment added successfully");
//        } else {
//            return ResponseEntity.status(400).body("Failed to add reply comment");
//        }
//    }

    @PostMapping("/reply")
    public ResponseEntity<String> replyComment(@RequestParam int parentCommentId, @RequestParam String content) {
        // 대댓글 작성 서비스 호출
        ArticleResult result = commentService.saveReplyComment(parentCommentId, content);

        if (result == ArticleResult.SUCCESS) {
            return ResponseEntity.ok("Reply comment added successfully");
        } else {
            return ResponseEntity.status(400).body("Failed to add reply comment");
        }
    }











    // 댓글 수정 기능
    @RequestMapping(value = "/", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchIndex(
            @RequestParam(value = "index", required = false, defaultValue = "0") int index,
            @RequestParam(value = "content", required = false) String content) {

        ModifyCommentResult result = this.commentService.modifyComment(index, content);
        JSONObject response = new JSONObject();
        response.put("result", result.name().toLowerCase());
        return response.toString();
    }

    // 댓글 삭제 기능
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteComment(@RequestParam(value = "commentId", required = false, defaultValue = "0") int commentId) {
        DeleteCommentResult result = this.commentService.deleteComment(commentId);
        JSONObject response = new JSONObject();
        response.put("result", result.name().toLowerCase());
        return response.toString();
    }




    // 댓글 불러오기 기능
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CommentEntity[]> getComments(@RequestParam(value = "postId", required = false, defaultValue = "0") int articleIndex) {
        CommentEntity[] comments = this.commentService.getCommentsByPostId(articleIndex);
        if (comments == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(comments);
    }


    // 대댓글 불러오기 엔드포인트
    @GetMapping("/replies")
    public ResponseEntity<CommentEntity[]> getReplies(@RequestParam int parentCommentId) {
        CommentEntity[] replies = commentService.getRepliesByParentId(parentCommentId);
        if (replies == null || replies.length == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(replies);
    }








}
