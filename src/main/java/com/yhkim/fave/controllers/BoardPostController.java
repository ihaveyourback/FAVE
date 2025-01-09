package com.yhkim.fave.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.entities.NotificationEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.mappers.NotificationMapper;
import com.yhkim.fave.results.LikedResult;

import com.yhkim.fave.services.BoardPostService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/board")
public class BoardPostController {

    private final BoardPostService boardPostService;
    private final WebSocketController webSocketController;


    // TODO 임시
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate brokerMessagingTemplate;

    @Autowired
    public BoardPostController(BoardPostService boardPostService, WebSocketController webSocketController, NotificationMapper notificationMapper, SimpMessagingTemplate brokerMessagingTemplate) {
        this.boardPostService = boardPostService;
        this.webSocketController = webSocketController;
        this.notificationMapper = notificationMapper;
        this.brokerMessagingTemplate = brokerMessagingTemplate;
    }

    // 좋아요 추가 처리
    @PostMapping("/like/{postId}")
    public LikedResult likePost(@PathVariable("postId") int postId) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;
        String userNickname = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserEntity) {
                UserEntity userEntity = (UserEntity) principal;
                userEmail = userEntity.getEmail();
                userNickname = userEntity.getNickname();
            }
        }
        if (userEmail == null) {
            return LikedResult.NOT_LOGGED_IN;
        }
        boolean result = boardPostService.addLike(postId); // 서비스가 로그인된 사용자 이메일을 처리

        if (result) {
            try {
                Map<String, String> messageData = new HashMap<>();

                webSocketController.sendTestMessage(messageData);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("알림 전송 실패: " + e.getMessage());
                // 실패 응답 반환 (필요 시)
                return LikedResult.FAILURE;
            }

            BoardPostEntity boardPost = this.boardPostService.getPostById(postId);
            NotificationEntity n = NotificationEntity.builder()
                    .userEmail(boardPost.getUserEmail())
                    .message(String.format("%s님이 게시글에 좋아요를 눌렀습니다.", userNickname))
                    .url(String.format("/article/read?index=%d", postId))
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            this.notificationMapper.insert(n);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            this.brokerMessagingTemplate.convertAndSend("/topic/alerts", new JSONObject(objectMapper.writeValueAsString(n)).toString());

            return LikedResult.SUCCESS;
        }

        return result ? LikedResult.SUCCESS : LikedResult.ALREADY_LIKED;

    }


    // 좋아요 삭제 처리
    @PostMapping("/unlike/{postId}")
    public LikedResult unlikePost(@PathVariable("postId") int postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // 로그인된 사용자의 이메일
        if (userEmail == null) {
            return LikedResult.NOT_LOGGED_IN;
        }
        boolean result = boardPostService.removeLike(postId); // 서비스가 로그인된 사용자 이메일을 처리
        return result ? LikedResult.SUCCESS : LikedResult.NOT_LIKED;
    }

    // 게시글의 좋아요 상태 및 좋아요 수 조회
    @GetMapping("/status/{postId}")
    public Map<String, Object> getPostLikeStatus(@PathVariable("postId") int postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // 로그인된 사용자의 이메일
        boolean isLiked = false;
        // 사용자가 좋아요를 눌렀는지 확인
        if (userEmail != null) {
            isLiked = boardPostService.isLiked(postId, userEmail);
        }
        // 해당 게시글의 좋아요 수 조회
        int likeCount = boardPostService.getLikeCount(postId);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);  // 좋아요 여부
        response.put("likeCount", likeCount); // 좋아요 수
        return response;
    }
}

