package com.yhkim.fave.services;

import com.yhkim.fave.entities.ArticleEntity;
import com.yhkim.fave.entities.ImageEntity;
import com.yhkim.fave.mappers.ArticleMapper;
import com.yhkim.fave.mappers.ImageMapper;
import com.yhkim.fave.results.article.ArticleResult;
import com.yhkim.fave.results.article.DeleteArticleResult;
import com.yhkim.fave.vos.ArticleVo;
import com.yhkim.fave.vos.PageVo_cy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final ImageMapper imageMapper;

    @Autowired
    public ArticleService(ArticleMapper articleMapper, ImageMapper imageMapper) {
        this.articleMapper = articleMapper;
        this.imageMapper = imageMapper;
    }

    /****************************************************************************/


    /******************************************************************************/
    // 삭제
    public DeleteArticleResult deleteArticle(int index) {
        if (index < 1) {
            return DeleteArticleResult.FAILURE;
        }

        ArticleEntity article = this.articleMapper.selectArticleByIndex(index);
        if (article == null) {
            return DeleteArticleResult.FAILURE;
        }

        if (article.getDeletedAt() != null) {
            return DeleteArticleResult.FAILURE;
        }

        article.setDeletedAt(LocalDateTime.now());
        return this.articleMapper.updateArticle(article) > 0
                ? DeleteArticleResult.SUCCESS
                : DeleteArticleResult.FAILURE;
    }



    public ImageEntity getImage(int index) {
        if (index < 1) {
            return null;
        }
        return this.imageMapper.selectImageByIndex(index);
    }

    public boolean modifyArticle(ArticleEntity article) {
        // 클라이언트가 준 데이터 유효성 검사
        if (article == null ||
                article.getIndex() < 1 ||
                article.getTitle() == null || article.getTitle().isEmpty() || article.getTitle().length() > 100 ||
                article.getContent() == null || article.getContent().isEmpty() || article.getContent().length() > 16_777_215) {
            return false;
        }

        ArticleEntity dbArticle = this.articleMapper.selectArticleByIndex(article.getIndex());
        // 데이터베이스에서 게시글 조회
        if (dbArticle == null || dbArticle.getDeletedAt() != null) {
            // 게시글이 존재하지 않거나 이미 삭제된 경우
            return false;
        }

        // 클라이언트가 전달한 데이터로 게시글 수정
        dbArticle.setTitle(article.getTitle());
        dbArticle.setContent(article.getContent());
        dbArticle.setUpdateAt(LocalDateTime.now()); // 수정 시간 갱신

        // 게시글 업데이트 결과 반환
        return this.articleMapper.updateArticle(dbArticle) > 0;
    }

//    public Pair<ArticleEntity[], PageVo_cy> searchArticles(String keyword, String filter, int page) {
//        int totalCount = articleMapper.selectArticleCountBySearch(filter, keyword);
//        PageVo_cy pageVo = new PageVo_cy(page, totalCount);
//        ArticleEntity[] articles = articleMapper.selectArticleBySearch(filter, keyword, pageVo.countPerPage, pageVo.offsetCount);
//        return Pair.of(articles, pageVo);
//    }
//
//    public Pair<ArticleEntity[], PageVo_cy> getArticlesByPaging(int page) {
//        int totalCount = this.articleMapper.getTotalArticlesCount();
//        PageVo_cy pageVo = new PageVo_cy(page, totalCount);
//        ArticleEntity[] articles = this.articleMapper.selectArticlesByPaging(pageVo.offsetCount, pageVo.countPerPage);
//
//        return Pair.of(articles, pageVo);
//    }

    public Pair<ArticleVo[], PageVo_cy> searchArticles(String keyword, String filter, int page) {
        int totalCount = articleMapper.selectArticleCountBySearch(filter, keyword);
        PageVo_cy pageVoCy = new PageVo_cy(page, totalCount);

        // 댓글 수를 포함한 게시물 조회
        ArticleVo[] articles = articleMapper.selectArticleBySearch(filter, keyword, pageVoCy.countPerPage, pageVoCy.offsetCount);;
        return Pair.of(articles, pageVoCy);
    }

    public Pair<ArticleVo[], PageVo_cy> getArticlesByPaging(int page) {
        int totalCount = this.articleMapper.getTotalArticlesCount();
        PageVo_cy pageVoCy = new PageVo_cy(page, totalCount);

        // 댓글 수를 포함한 페이징된 게시물 조회
        ArticleVo[] articles = this.articleMapper.selectArticlesByPaging(pageVoCy.offsetCount, pageVoCy.countPerPage);

        return Pair.of(articles, pageVoCy);
    }


    public boolean increaseArticleView(ArticleEntity article) {
        if (article == null) {
            return false;
        }
        article.setView(article.getView() + 1);
        return this.articleMapper.updateArticle(article) > 0;
    }

    public ArticleEntity getArticleByIndex(int index) {
        return articleMapper.selectArticleByIndex(index);
    }



    public boolean uploadImage(ImageEntity image) {
        if (image == null ||
                image.getData() == null ||
                image.getData().length == 0 ||
                image.getContentType() == null || image.getContentType().isEmpty() ||
                image.getName() == null || image.getName().isEmpty()) {
            return false;
        }
        image.setCreatedAt(LocalDateTime.now());
        return this.imageMapper.insertImage(image) > 0;
    }

    public ArticleResult write(ArticleEntity article) {
        if (article == null ||
                article.getTitle() == null || article.getTitle().isEmpty() || article.getTitle().length() > 100 ||
                article.getContent() == null || article.getContent().isEmpty()) {
            return ArticleResult.FAILURE;
        }

        article.setCreatedAt(LocalDateTime.now());
        article.setUpdateAt(null);
        article.setDeletedAt(null);
//        article.setUserEmail("yellow6480@gmail.com");
//        article.setUserNickname("관리자");
        article.setUserEmail("yellow077@naver.com");
        article.setUserNickname("김두루미");


        return this.articleMapper.insertArticle(article) > 0
                ? ArticleResult.SUCCESS
                : ArticleResult.FAILURE;
    }
}
