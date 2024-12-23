package com.yhkim.fave.mappers;

import com.cykim.teamproject.entities.CommentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
    int insertComment(CommentEntity comment);

    CommentEntity[] selectCommentsByPostId(@Param("postId")int postId);

    int updateComment(CommentEntity comment);

    CommentEntity selectCommentByIndex(@Param("index") int index);

    // 대댓글 불러오기
    CommentEntity[] selectRepliesByParentId(@Param("parentCommentId") int parentCommentId);
}
