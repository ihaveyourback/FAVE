package com.yhkim.fave.services;

import com.yhkim.fave.entities.CommentEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.mappers.CommentMapper;
import com.yhkim.fave.results.article.ArticleResult;
import com.yhkim.fave.results.comment.DeleteCommentResult;
import com.yhkim.fave.results.comment.ModifyCommentResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {
    private final CommentMapper commentMapper;

    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    // 댓글 수정 기능
    public ModifyCommentResult modifyComment(int index, String content) {
        if (index < 1 || content == null || content.isEmpty() || content.length() > 100) {
            return ModifyCommentResult.FAILURE;
        }
        CommentEntity comment = this.commentMapper.selectCommentByIndex(index);
        if (comment == null || comment.getIsDeleted() != null) {
            return ModifyCommentResult.FAILURE;
        }

        // 로그인된 사용자 이메일과 댓글 작성자의 이메일 비교
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserEntity) {
                UserEntity userEntity = (UserEntity) principal;
                userEmail = userEntity.getEmail();
            }
        }

        if (userEmail == null || !userEmail.equals(comment.getUserEmail())) {
            return ModifyCommentResult.FAILURE; // 다른 사용자의 댓글은 수정할 수 없음
        }

        comment.setComment(content);
        comment.setUpdateAt(LocalDateTime.now());
        return this.commentMapper.updateComment(comment) > 0
                ? ModifyCommentResult.SUCCESS
                : ModifyCommentResult.FAILURE;
    }

    // 댓글 삭제
    public DeleteCommentResult deleteComment(int index) {
        if (index < 1) {
            return DeleteCommentResult.FAILURE; // 유효하지 않은 index
        }

        CommentEntity comment = this.commentMapper.selectCommentByIndex(index);
        if (comment == null) {
            return DeleteCommentResult.FAILURE; // 댓글이 없음
        }

        if (comment.getIsDeleted() != null) {
            return DeleteCommentResult.FAILURE; // 이미 삭제된 댓글
        }

        // 로그인된 사용자 이메일과 댓글 작성자의 이메일 비교
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserEntity) {
                UserEntity userEntity = (UserEntity) principal;
                userEmail = userEntity.getEmail();
            }
        }

        if (userEmail == null || !userEmail.equals(comment.getUserEmail())) {
            return DeleteCommentResult.FAILURE; // 다른 사용자의 댓글은 삭제할 수 없음
        }

        comment.setIsDeleted(LocalDateTime.now()); // 삭제 시간 설정
        int updateCount = this.commentMapper.updateComment(comment);

        // 부모 댓글이 삭제되면 모든 자식 댓글도 삭제
        updateCount += deleteRepliesAndSubReplies(index);

        return updateCount > 0 ? DeleteCommentResult.SUCCESS : DeleteCommentResult.FAILURE;
    }

    // 자식 댓글들을 재귀적으로 삭제하는 함수
    private int deleteRepliesAndSubReplies(int parentId) {
        int updateCount = 0;

        // 해당 부모 댓글에 달린 대댓글을 가져옵니다.
        CommentEntity[] replies = this.commentMapper.selectRepliesByParentId(parentId);
        for (CommentEntity reply : replies) {
            if (reply.getIsDeleted() == null) {
                reply.setIsDeleted(LocalDateTime.now()); // 삭제 시간 설정
                updateCount += this.commentMapper.updateComment(reply);
                // 재귀적으로 대댓글이 있을 경우 다시 삭제
                updateCount += deleteRepliesAndSubReplies(reply.getIndex());
            }
        }

        return updateCount;
    }

    // 댓글 불러오기 기능
    public CommentEntity[] getCommentsByPostId(int articleIndex) {
        if (articleIndex < 1) {
            return new CommentEntity[0];
        }
        CommentEntity[] commentEntities = this.commentMapper.selectCommentsByPostId(articleIndex);
        if (commentEntities == null || commentEntities.length == 0) {
            return new CommentEntity[0];
        }
        return commentEntities;
    }

    // 댓글 작성
    public ArticleResult writeComment(CommentEntity comment) {
        if (comment == null ||
                comment.getComment() == null || comment.getComment().isEmpty() || comment.getComment().length() > 1000) {
            System.out.println("Comment validation failed");
            return ArticleResult.FAILURE;
        }
        System.out.println("Starting authentication check...");
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

        System.out.println("userEmail: " + userEmail);
        System.out.println("userNickname: " + userNickname);

        if (userEmail == null) {
            System.out.println("Authentication failed. Returning FAILURE.");
            return ArticleResult.FAILURE;
        }

        comment.setCreatedAt(LocalDateTime.now());
        comment.setUserEmail(userEmail);
        comment.setUserNickname(userNickname != null ? userNickname : "익명");

        int result = commentMapper.insertComment(comment);

        return result > 0 ? ArticleResult.SUCCESS : ArticleResult.FAILURE;
    }

    // 대댓글 작성
    public ArticleResult saveReplyComment(int parentCommentId, String content) {
        if (parentCommentId < 1 || content == null || content.isEmpty() || content.length() > 100) {
            return ArticleResult.FAILURE;
        }
        CommentEntity parentComment = this.commentMapper.selectCommentByIndex(parentCommentId);
        if (parentComment == null || parentComment.getIsDeleted() != null) {
            return ArticleResult.FAILURE;
        }
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
            return ArticleResult.FAILURE;
        }

        CommentEntity replyComment = new CommentEntity();
        replyComment.setPostId(parentComment.getPostId());
        replyComment.setCommentId(parentCommentId);
        replyComment.setComment(content);
        replyComment.setCreatedAt(LocalDateTime.now());
        replyComment.setUpdateAt(null);
        replyComment.setIsDeleted(null);
        replyComment.setUserEmail(userEmail);
        replyComment.setUserNickname(userNickname != null ? userNickname : "익명");

        return this.commentMapper.insertComment(replyComment) > 0 ? ArticleResult.SUCCESS : ArticleResult.FAILURE;
    }

    // 부모 댓글에 대한 대댓글 목록 조회
    public CommentEntity[] getRepliesByParentId(int parentCommentId) {
        if (parentCommentId < 1) {
            return new CommentEntity[0];
        }
        return this.commentMapper.selectRepliesByParentId(parentCommentId);
    }
}


