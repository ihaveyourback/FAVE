package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.vos.PageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardPostMapper {
    List<BoardPostEntity> selectPostsByUserEmail(@Param("userEmail") String userEmail, @Param("pageVo") PageVo pageVo);
    // 사용자 이메일로 게시물 목록 조회 (페이징 처리)

    int countPostsByUserEmail(String userEmail);
    // 사용자 이메일로 게시물 수 조회
}