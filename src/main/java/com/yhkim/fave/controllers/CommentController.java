package com.yhkim.fave.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yhkim.fave.entities.*;
import com.yhkim.fave.mappers.NotificationMapper;
import com.yhkim.fave.results.article.ArticleResult;
import com.yhkim.fave.results.comment.DeleteCommentResult;
import com.yhkim.fave.results.comment.ModifyCommentResult;
import com.yhkim.fave.services.BoardPostService;
import com.yhkim.fave.services.CommentService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/comment")
public class CommentController {
    private final CommentService commentService;
    private final WebSocketController webSocketController;
    private final BoardPostService boardPostService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate brokerMessagingTemplate;

    @Autowired
    public CommentController(CommentService commentService, WebSocketController webSocketController, BoardPostService boardPostService, NotificationMapper notificationMapper, SimpMessagingTemplate brokerMessagingTemplate) {
        this.commentService = commentService;
        this.webSocketController = webSocketController;
        this.boardPostService = boardPostService;
        this.notificationMapper = notificationMapper;
        this.brokerMessagingTemplate = brokerMessagingTemplate;
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
    public String postIndex(CommentEntity comment, Authentication authentication,
                            @RequestParam Map<String, String> messageData,
                            @RequestParam("postId") Integer postId,
                            @AuthenticationPrincipal UserEntity user) throws JsonProcessingException {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "{\"result\":\"FAILURE\"}"; // 인증되지 않은 사용자 처리
        }

        // 인증된 사용자 정보 가져오기
        String userEmail = null;
        String userNickname = null;

        if (authentication.getPrincipal() instanceof UserEntity) {
            // 일반 로그인 사용자 처리
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userEmail = userEntity.getEmail();
            userNickname = userEntity.getNickname(); // UserEntity에 닉네임 필드가 있다고 가정
        } else if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            // 소셜 로그인 사용자 처리
            CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
            userEmail = oauthUser.getName(); // 소셜 로그인 이메일
            userNickname = (String) oauthUser.getAttributes().getOrDefault("nickname", "사용자"); // 소셜 로그인 닉네임
        }

        // 댓글 작성 처리
        ArticleResult result = this.commentService.writeComment(comment, authentication);

        // 댓글 작성이 성공적인 경우
        if (result == ArticleResult.SUCCESS) {
            // 댓글 작성자 정보
            // 메시지와 알림 메시지 전달
            try {
                // WebSocket 메서드 호출
                webSocketController.sendTestMessage(messageData);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("알림 전송 실패");
            }

            // 게시글 정보 가져오기
            BoardPostEntity boardPost = this.boardPostService.getPostById(postId);
            String postTitle = boardPost.getTitle();

            // 알림 생성
            NotificationEntity n = NotificationEntity.builder()
                    .userEmail(boardPost.getUserEmail()) // 게시글 작성자에게 알림
                    .message(String.format("%s님이 %s 댓글을 달았습니다.", userNickname, postTitle))
                    .url(String.format("/article/read?index=%d", postId))
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            // 알림 저장
            this.notificationMapper.insert(n);

            // WebSocket을 통해 실시간 알림 전송
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            this.brokerMessagingTemplate.convertAndSend("/topic/alerts", new JSONObject(objectMapper.writeValueAsString(n)).toString());

            // 결과 반환
            JSONObject response = new JSONObject();
            response.put("result", result);
            return response.toString();
        }

        // 댓글 작성 실패 시 처리
        return ArticleResult.FAILURE.toString();
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
    public ResponseEntity<Map<String, Object>> replyComment(
            @RequestParam int parentCommentId,
            @RequestParam String content,
            @RequestParam(value = "commentAuthor", required = false) String commentAuthor,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "index", required = false) Integer index,
            @RequestParam(value = "postId", required = false) Integer postId) throws JsonProcessingException {

        Map<String, Object> response = new HashMap<>();

        // 사용자 정보 가져오기
        String userEmail = null;
        String userNickname = "익명"; // 기본값 설정

        if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            userEmail = customOAuth2User.getEmail();
            userNickname = customOAuth2User.getNickname();
        } else if (principal instanceof UserEntity) {
            UserEntity userEntity = (UserEntity) principal;
            userEmail = userEntity.getEmail();
            userNickname = userEntity.getNickname();
        }

        if (userEmail == null) {
            response.put("result", "failure");
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 댓글 저장 로직
        ArticleResult result = commentService.saveReplyComment(parentCommentId, content, userEmail, userNickname);

        if (result == ArticleResult.SUCCESS) {
            // WebSocket 메서드 호출
            try {
                Map<String, String> messageData = new HashMap<>();
                messageData.put("parentCommentId", String.valueOf(parentCommentId));
                messageData.put("content", content);
                messageData.put("nickname", userNickname);
                messageData.put("commentAuthor", commentAuthor);
                messageData.put("notificationType", "대댓글");

                webSocketController.sendTestMessage(messageData);
            } catch (Exception e) {
                System.out.println("WebSocket 알림 전송 실패: " + e.getMessage());
                e.printStackTrace();

                response.put("result", "failure");
                response.put("message", "WebSocket 알림 전송 실패");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 부모 댓글 작성자 정보 가져오기
            CommentEntity commentPost = this.commentService.getSelectCommentsByParentId(parentCommentId);

            String replyComment = commentPost.getComment();

            // 알림 생성 및 저장
            NotificationEntity n = NotificationEntity.builder()
                    .userEmail(commentPost.getUserEmail()) // 부모 댓글 작성자에게 알림
                    .message(String.format("%s님이 %s에 대댓글을 달았습니다.", userNickname,replyComment))
                    .url(postId != null ? String.format("/article/read?index=%d", postId) : "/")
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();


            notificationMapper.insert(n);

            // WebSocket으로 알림 전송
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            brokerMessagingTemplate.convertAndSend("/topic/alerts", new JSONObject(objectMapper.writeValueAsString(n)).toString());

            response.put("result", "success");
            response.put("message", "대댓글 작성 성공");
            return ResponseEntity.ok(response);
        }

        response.put("result", "failure");
        response.put("message", "대댓글 작성 실패");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @RequestMapping(value = "/", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchIndex (
            @RequestParam(value = "index", required = false, defaultValue = "0") int index,
            @RequestParam(value = "content", required = false) String content,
            @AuthenticationPrincipal Object principal){

        // 로그인된 사용자의 이메일과 닉네임 가져오기
        String userEmail = null;
        if (principal instanceof UserEntity) {
            UserEntity userEntity = (UserEntity) principal;
            userEmail = userEntity.getEmail();
        } else if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            userEmail = customOAuth2User.getEmail();
        }

        // 로그인되지 않았거나 이메일 정보가 없는 경우
        if (userEmail == null) {
            return "{\"result\":\"failure\", \"message\":\"로그인이 필요합니다.\"}";
        }

        // 댓글 수정 서비스 호출
        ModifyCommentResult result = this.commentService.modifyComment(index, content, userEmail);

        JSONObject response = new JSONObject();
        response.put("result", result.name().toLowerCase());
        return response.toString();
    }


    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteComment(@RequestParam(value = "commentId", required = false, defaultValue = "0") int commentId,
                                @AuthenticationPrincipal Object principal) {
        String userEmail = null;
        if (principal instanceof UserEntity userEntity) {
            userEmail = userEntity.getEmail();
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            userEmail = customOAuth2User.getEmail();
        }

        if (userEmail == null) {
            return "{\"result\":\"failure\", \"message\":\"로그인이 필요합니다.\"}";
        }

        // 댓글 삭제 서비스 호출
        DeleteCommentResult result = this.commentService.deleteComment(commentId, userEmail.toLowerCase());
        JSONObject response = new JSONObject();
        response.put("result", result.name().toLowerCase());
        return response.toString();
    }


    // 댓글 불러오기 기능
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CommentEntity[]> getComments (
            @RequestParam(value = "postId", required = false, defaultValue = "0") int articleIndex){
        CommentEntity[] comments = this.commentService.getCommentsByPostId(articleIndex);
        if (comments == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(comments);
    }


    // 대댓글 불러오기 엔드포인트
    @GetMapping("/replies")
    public ResponseEntity<CommentEntity[]> getReplies ( @RequestParam int parentCommentId){
        CommentEntity[] replies = commentService.getRepliesByParentId(parentCommentId);
        if (replies == null || replies.length == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(replies);
    }


}
