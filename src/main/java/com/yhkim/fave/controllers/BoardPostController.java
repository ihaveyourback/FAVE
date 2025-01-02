package com.yhkim.fave.controllers;

import com.yhkim.fave.results.LikedResult;
import com.yhkim.fave.services.BoardPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardPostController {

    private final BoardPostService boardPostService;

    // 좋아요 추가 처리
    @PostMapping("/like/{postId}")
    public LikedResult likePost(@PathVariable("postId") int postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // 로그인된 사용자의 이메일
        if (userEmail == null) {
            return LikedResult.NOT_LOGGED_IN;
        }
        boolean result = boardPostService.addLike(postId); // 서비스가 로그인된 사용자 이메일을 처리
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
