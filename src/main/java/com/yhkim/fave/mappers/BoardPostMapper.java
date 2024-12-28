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

//    김범수 //

    BoardPostEntity[] selectBoardPosts();

    int selectBoardPostCount();

    BoardPostEntity[] selectBoardPost(@Param(value = "limitCount") int limitCount,
                                       @Param(value = "offsetCount") int offsetCount);

    int selectBoardPostCountBySearch(@Param(value = "filter") String filter,
                                     @Param(value = "keyword") String keyword);

    BoardPostEntity[] selectBoardPostBySearch(@Param(value = "filter") String filter,
                                               @Param(value = "keyword") String keyword,
                                               @Param(value = "limitCount") int limitCount,
                                               @Param(value = "offsetCount") int offsetCount);

    BoardPostEntity selectBoardPostsByIndex(@Param("index") int index);

    int updateBoardPost(BoardPostEntity board);
}