package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.InquiriesArticleEntity;
import com.yhkim.fave.vos.InquiriesArticleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InquiriesArticleMapper {
    int insertArticle(InquiriesArticleEntity inquiries);

    int updateArticle(InquiriesArticleEntity inquiries);

    InquiriesArticleEntity selectArticleByIndex(@Param("index") int index);

    InquiriesArticleEntity[] selectArticles();

    int getTotalArticlesCount();

    InquiriesArticleVo[] selectArticlesByPaging(@Param("offsetCount") int offsetCount, @Param("countPerPage") int countPerPage);
    ;

    int selectArticleCountBySearch(@Param("filter") String filter, @Param("keyword") String keyword);

    InquiriesArticleVo[] selectArticleBySearch(
            @Param("filter") String filter,
            @Param("keyword") String keyword,
            @Param("limitCount") int limitCount,
            @Param("offsetCount") int offsetCount);
}
