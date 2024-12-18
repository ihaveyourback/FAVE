package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.BoardPostsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardPostsMapper {
    int selectBoardPostCount();

    BoardPostsEntity[] selectBoardPost(@Param(value = "limitCount") int limitCount,
                                       @Param(value = "offsetCount") int offsetCount);

    int selectBoardPostCountBySearch(@Param(value = "filter") String filter,
                                     @Param(value = "keyword") String keyword);

    BoardPostsEntity[] selectBoardPostBySearch(@Param(value = "filter") String filter,
                                               @Param(value = "keyword") String keyword,
                                               @Param(value = "limitCount") int limitCount,
                                               @Param(value = "offsetCount") int offsetCount);

    BoardPostsEntity selectBoardPostsByIndex(@Param("index") int index);

    int updateBoardPost(BoardPostsEntity board);
}
