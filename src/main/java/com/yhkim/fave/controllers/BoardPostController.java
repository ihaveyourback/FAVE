package com.yhkim.fave.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.NotificationEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.results.LikedResult;
import com.yhkim.fave.services.BoardPostService;
import com.yhkim.fave.mappers.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardPostController {

    private final BoardPostService boardPostService;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate brokerMessagingTemplate;

    @PostMapping("/like/{postId}")
    public ResponseEntity<LikedResult> likePost(@PathVariable("postId") int postId) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;
        String userNickname = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserEntity) {
                // 일반 로그인 사용자
                UserEntity userEntity = (UserEntity) principal;
                userEmail = userEntity.getEmail();
                userNickname = userEntity.getNickname(); // UserEntity에 닉네임 필드가 있다고 가정
            } else if (principal instanceof CustomOAuth2User) {
                // 소셜 로그인 사용자
                CustomOAuth2User oauthUser = (CustomOAuth2User) principal;
                userEmail = oauthUser.getName(); // 소셜 로그인 이메일
                userNickname = oauthUser.getNickname(); // CustomOAuth2User에서 닉네임 가져오기
            }
        }

        if (userEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LikedResult.NOT_LOGGED_IN);
        }

        boolean result = boardPostService.addLike(postId);
        if (result) {
            // 게시글 작성자 정보 가져오기
            BoardPostEntity boardPost = this.boardPostService.getPostById(postId);

            // 좋아요를 누른 사용자가 게시글 작성자와 같은지 확인
            if (userEmail.equals(boardPost.getUserEmail())) {
                // 본인이 본인의 게시글에 좋아요를 눌렀을 때 알림을 보내지 않음
                return ResponseEntity.ok(LikedResult.SUCCESS);
            }

            String postTitle = boardPost.getTitle();
            NotificationEntity notification = NotificationEntity.builder()
                    .userEmail(boardPost.getUserEmail()) // 게시글 작성자의 이메일
                    .message(String.format("%s님이 %s에 좋아요를 눌렀습니다.", userNickname, postTitle))
                    .url(String.format("/article/read?index=%d", postId)) // 알림 클릭 시 이동할 URL
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            this.notificationMapper.insert(notification);

            // WebSocket 실시간 알림 전송
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            this.brokerMessagingTemplate.convertAndSend("/topic/alerts", new JSONObject(objectMapper.writeValueAsString(notification)).toString());

            return ResponseEntity.ok(LikedResult.SUCCESS);
        }

        return ResponseEntity.ok(LikedResult.ALREADY_LIKED);
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