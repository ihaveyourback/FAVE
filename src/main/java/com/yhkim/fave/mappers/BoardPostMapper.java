package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.BoardPostEntity;
import com.yhkim.fave.vos.PageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardPostMapper {
    List<BoardPostEntity> selectPostsByUserEmail(@Param("userEmail") String userEmail, @Param("pageVo") PageVo pageVo);

    int countPostsByUserEmail(String userEmail);
}