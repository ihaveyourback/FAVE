package com.yhkim.fave.mappers;

import com.yhkim.fave.entities.ArticleEntity;
import com.yhkim.fave.vos.ArticleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {
    int insertArticle(ArticleEntity article);

    int updateArticle(ArticleEntity article);

    ArticleEntity selectArticleByIndex(@Param("index") int index);

    ArticleEntity[] selectArticles();

    int getTotalArticlesCount();

    ArticleVo[] selectArticlesByPaging(@Param("offsetCount") int offsetCount, @Param("countPerPage") int countPerPage);
    ;

    int selectArticleCountBySearch(@Param("filter") String filter, @Param("keyword") String keyword);

    ArticleVo[] selectArticleBySearch(
            @Param("filter") String filter,
            @Param("keyword") String keyword,
            @Param("limitCount") int limitCount,
            @Param("offsetCount") int offsetCount);
}
