package com.yhkim.fave.services;

//import com.lsm.declaration.detail.CustomUserDetails;
import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.ImageEntity;
import com.yhkim.fave.entities.InquiriesArticleEntity;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.mappers.ImageMapper;
import com.yhkim.fave.mappers.InquiriesArticleMapper;
import com.yhkim.fave.results.article.ArticleResult;
import com.yhkim.fave.results.article.DeleteArticleResult;
import com.yhkim.fave.vos.InquiriesArticleVo;
import com.yhkim.fave.vos.PageVo_cy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class InquiriesArticleService {
    private final InquiriesArticleMapper inquiriesArticleMapper;
    private final ImageMapper imageMapper;

    @Autowired
    public InquiriesArticleService(InquiriesArticleMapper inquiriesArticleMapper, ImageMapper imageMapper) {
        this.inquiriesArticleMapper = inquiriesArticleMapper;
        this.imageMapper = imageMapper;
    }

    // 삭제
    public DeleteArticleResult deleteArticle(int index) {
        if (index < 1) {
            return DeleteArticleResult.FAILURE;
        }

        InquiriesArticleEntity article = this.inquiriesArticleMapper.selectArticleByIndex(index);
        if (article == null) {
            return DeleteArticleResult.FAILURE;
        }

        if (article.getIsDeleted() != null) {
            return DeleteArticleResult.FAILURE;
        }

        article.setIsDeleted(LocalDateTime.now());
        return this.inquiriesArticleMapper.updateArticle(article) > 0
                ? DeleteArticleResult.SUCCESS
                : DeleteArticleResult.FAILURE;
    }



    public ImageEntity getImage(int index) {
        if (index < 1) {
            return null;
        }
        return this.imageMapper.selectImageByIndex(index);
    }

    public boolean modifyArticle(InquiriesArticleEntity article) {
        // 클라이언트가 준 데이터 유효성 검사
        if (article == null ||
                article.getIndex() < 1 ||
                article.getTitle() == null || article.getTitle().isEmpty() || article.getTitle().length() > 100 ||
                article.getContent() == null || article.getContent().isEmpty() || article.getContent().length() > 16_777_215) {
            return false;
        }

        InquiriesArticleEntity dbArticle = this.inquiriesArticleMapper.selectArticleByIndex(article.getIndex());
        // 데이터베이스에서 게시글 조회
        if (dbArticle == null || dbArticle.getIsDeleted() != null) {
            // 게시글이 존재하지 않거나 이미 삭제된 경우
            return false;
        }

        // 클라이언트가 전달한 데이터로 게시글 수정
        dbArticle.setTitle(article.getTitle());
        dbArticle.setContent(article.getContent());
        dbArticle.setUpdateAt(LocalDateTime.now()); // 수정 시간 갱신

        // 게시글 업데이트 결과 반환
        return this.inquiriesArticleMapper.updateArticle(dbArticle) > 0;
    }

    public Pair<InquiriesArticleVo[], PageVo_cy> searchArticles(String keyword, String filter, int page) {
        int totalCount = inquiriesArticleMapper.selectArticleCountBySearch(filter, keyword);
        PageVo_cy pageVoCy = new PageVo_cy(page, totalCount);

        // 댓글 수를 포함한 게시물 조회
        InquiriesArticleVo[] articles = inquiriesArticleMapper.selectArticleBySearch(filter, keyword, pageVoCy.countPerPage, pageVoCy.offsetCount);;
        return Pair.of(articles, pageVoCy);
    }

    public Pair<InquiriesArticleVo[], PageVo_cy> getArticlesByPaging(int page) {
        int totalCount = this.inquiriesArticleMapper.getTotalArticlesCount();
        PageVo_cy pageVoCy = new PageVo_cy(page, totalCount);

        // 댓글 수를 포함한 페이징된 게시물 조회
        InquiriesArticleVo[] articles = this.inquiriesArticleMapper.selectArticlesByPaging(pageVoCy.offsetCount, pageVoCy.countPerPage);

        return Pair.of(articles, pageVoCy);
    }


    public boolean increaseArticleView(InquiriesArticleEntity article) {
        if (article == null) {
            return false;
        }
        article.setView(article.getView() + 1);
        return this.inquiriesArticleMapper.updateArticle(article) > 0;
    }

    public InquiriesArticleEntity getArticleByIndex(int index) {
        return inquiriesArticleMapper.selectArticleByIndex(index);
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

    public ArticleResult write(InquiriesArticleEntity article) {
        // 1. 입력 검증: 게시글이 null이 아니고, 제목과 내용이 유효한지 체크
        if (article == null ||
                article.getTitle() == null || article.getTitle().isEmpty() || article.getTitle().length() > 100 ||
                article.getContent() == null || article.getContent().isEmpty()) {
            // 유효하지 않으면 실패 반환
            return ArticleResult.FAILURE;
        }

        // 2. 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;
        String userNickname = null;

        // 3. 사용자가 인증된 경우, 이메일과 닉네임 추출
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            // 4. 소셜 로그인 (CustomOAuth2User) 처리
            if (principal instanceof CustomOAuth2User) {
                CustomOAuth2User customUser = (CustomOAuth2User) principal;
                userEmail = customUser.getEmail(); // 이메일 추출
                userNickname = customUser.getNickname(); // 닉네임 추출
            }
            // 5. 일반 로그인 (UsernamePasswordAuthenticationToken) 처리
            else if (principal instanceof UserEntity) {
                UserEntity userEntity = (UserEntity) principal;
                userEmail = userEntity.getEmail(); // 이메일 추출
                userNickname = userEntity.getNickname(); // 닉네임 추출
            }
        }

        // 6. 사용자 정보 출력 (디버깅 용도)
        System.out.println("userEmail: " + userEmail);
        System.out.println("userNickname: " + userNickname);

        // 7. 이메일 정보가 없다면 인증 실패로 간주하고, 실패 반환
        if (userEmail == null) {
            System.out.println("Authentication failed. Returning FAILURE.");
            return ArticleResult.FAILURE;
        }

        // 8. 게시글의 생성일시와 사용자 정보 설정
        article.setCreateAt(LocalDateTime.now()); // 현재 시간으로 생성일시 설정
        article.setUserEmail(userEmail); // 사용자 이메일 설정
        article.setUserNickname(userNickname != null ? userNickname : "익명"); // 닉네임 설정 (null이면 "익명"으로 설정)

        // 9. 게시글을 데이터베이스에 삽입
        int result = this.inquiriesArticleMapper.insertArticle(article);

        // 10. 삽입 결과가 성공적이면 SUCCESS 반환, 아니면 FAILURE 반환
        return result > 0 ? ArticleResult.SUCCESS : ArticleResult.FAILURE;
    }

}
