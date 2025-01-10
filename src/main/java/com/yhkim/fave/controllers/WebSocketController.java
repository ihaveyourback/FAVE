package com.yhkim.fave.controllers;


import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.NotificationEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.mappers.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.util.HashMap;
import java.util.Map;

@Controller
@EnableWebSocket
@EnableWebSocketMessageBroker
@RequestMapping("/api/notifications")
public class WebSocketController {
    private final NotificationMapper notificationMapper;

    @Autowired
    public WebSocketController(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @MessageMapping("/sendTestMessage")
    @SendTo("/topic/alerts")
    public Map<String, String> sendTestMessage(@RequestBody Map<String, String> payload) {
        // 요청에서 데이터 추출
        String message = payload.get("message");
        String notificationMessage = payload.get("notificationMessage");
        String nickname = payload.get("nickname");
        String postAuthor = payload.get("postAuthor"); // 게시글 작성자
        String commentAuthor = payload.get("commentAuthor"); // 댓글 작성자 (대댓글 알림용)
        String postTitle = payload.get("postTitle");
        String articleIndex = payload.get("articleIndex");
        String commentContent = payload.get("commentContent");
        String notificationType = payload.get("notificationType");


        // 응답 데이터 생성
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("notificationMessage", notificationMessage);
        response.put("nickname", nickname);
        response.put("postAuthor", postAuthor); // 게시글 작성자
        response.put("commentAuthor", commentAuthor); // 댓글 작성자 추가
        response.put("postTitle", postTitle);
        response.put("articleIndex", articleIndex);
        response.put("commentContent", commentContent);
        response.put("notificationType", notificationType);

        return response;
    }


    @PostMapping("/unread")
    public ResponseEntity<NotificationEntity[]> getUnreadNotifications(@RequestBody Map<String, String> request) {
        String userEmail = request.get("userEmail");
        if (userEmail != null) {
            // 수정된 Native Query를 호출
            NotificationEntity[] notifications = this.notificationMapper.selectAll(userEmail);
            return ResponseEntity.ok(notifications);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new NotificationEntity[0]);
    }

    @PatchMapping("/all")
    @ResponseBody
    public void patchAll(@AuthenticationPrincipal UserEntity user, @AuthenticationPrincipal Object principal) {
        String currentUserEmail = null;

        if (user != null) {
            // 일반 로그인 사용자
            currentUserEmail = user.getEmail();
        } else if (principal instanceof CustomOAuth2User) {
            // 소셜 로그인 사용자 처리
            CustomOAuth2User oauthUser = (CustomOAuth2User) principal;
            currentUserEmail = oauthUser.getName(); // 이메일 또는 ID로 사용자 정보 반환
        }

        if (currentUserEmail == null) {
            throw new IllegalStateException("사용자 정보를 확인할 수 없습니다.");
        }

        NotificationEntity[] ns = this.notificationMapper.selectAll(currentUserEmail);
        for (NotificationEntity n : ns) {
            n.setRead(true);
            this.notificationMapper.update(n);
        }
    }

    @DeleteMapping(value = "/")
    @ResponseBody
    public ResponseEntity<Void> deleteIndex(
            @AuthenticationPrincipal UserEntity user,
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "index", required = false) int index) {

        String currentUserEmail = null;

        if (user != null) {
            // 일반 로그인 사용자
            currentUserEmail = user.getEmail();
        } else if (principal instanceof CustomOAuth2User) {
            // 소셜 로그인 사용자 처리
            CustomOAuth2User oauthUser = (CustomOAuth2User) principal;
            currentUserEmail = oauthUser.getName(); // 이메일 또는 ID로 사용자 정보 반환
        }

        if (currentUserEmail == null || index < 1) {
            return ResponseEntity.badRequest().build();
        }

        NotificationEntity n = this.notificationMapper.select(index);
        if (n == null || !n.getUserEmail().equals(currentUserEmail)) {
            return ResponseEntity.badRequest().build();
        }

        n.setDeleted(true);
        this.notificationMapper.update(n);
        return ResponseEntity.ok().build();
    }
}
