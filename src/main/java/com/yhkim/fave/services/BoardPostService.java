package com.yhkim.fave.services;

import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.mappers.BoardPostMapper;
import com.yhkim.fave.vos.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardPostService {
    private final BoardPostMapper boardPostMapper;

    @Autowired
    public BoardPostService(BoardPostMapper boardPostMapper) {
        this.boardPostMapper = boardPostMapper;
    }

    public List<BoardPostEntity> getPostsByUserEmail(String userEmail, PageVo pageVo) {
        return boardPostMapper.selectPostsByUserEmail(userEmail, pageVo); // 사용자의 게시물 목록 가져오기 (페이징 처리)
    }

    public int countPostsByUserEmail(String userEmail) {
        return boardPostMapper.countPostsByUserEmail(userEmail); // 사용자의 게시물 수
    }


    // 김창윤 //

    // 로그인된 사용자의 이메일을 가져오는 메서드
    private String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // 로그인된 사용자의 이메일
    }

    @Transactional
    // 좋아요 추가
    public boolean addLike(int postId) {
        // 로그인된 사용자의 이메일을 가져옴
        String userEmail = getLoggedInUserEmail();

        // 이미 좋아요 상태인지 확인
        if (boardPostMapper.isLiked(postId, userEmail)) {
            return false; // 중복 삽입 방지
        }

        int insertedRows = boardPostMapper.addLike(postId, userEmail);
        if (insertedRows > 0) {
            boardPostMapper.incrementLikeCount(postId);
            return true;
        }
        return false;
    }

    @Transactional
    // 좋아요 삭제
    public boolean removeLike(int postId) {
        // 로그인된 사용자의 이메일을 가져옴
        String userEmail = getLoggedInUserEmail();

        // 이미 좋아요 상태인지 확인
        if (!boardPostMapper.isLiked(postId, userEmail)) {
            return false; // 중복 삽입 방지
        }

        int deletedRows = boardPostMapper.removeLike(postId, userEmail);
        if (deletedRows > 0) {
            boardPostMapper.decrementLikeCount(postId);
            return true;
        }
        return false;
    }

    // 게시글의 좋아요 여부 조회
    public boolean isLiked(int postId, String userEmail) {
        return boardPostMapper.isLiked(postId, userEmail);
    }

    // 게시글의 좋아요 수 조회
    public int getLikeCount(int postId) {
        return boardPostMapper.getLikeCount(postId);
    }
}