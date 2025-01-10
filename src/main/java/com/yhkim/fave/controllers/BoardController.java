package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.services.ArticleService;
import com.yhkim.fave.vos.ArticleVo;
import com.yhkim.fave.vos.PageVo;
import com.yhkim.fave.vos.PageVo_cy;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/board")
public class BoardController {

    private final ArticleService articleService;

    @Autowired
    public BoardController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // 게시글 목록 조회
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "keyword", required = false) String keyword,
            @AuthenticationPrincipal UserDetails userDetails,
            @AuthenticationPrincipal Object principal) {

        ModelAndView modelAndView = new ModelAndView();
        if (userDetails instanceof UserEntity user) {// 사용자 정보가 UserEntity 객체인 경우
            modelAndView.addObject("user", user); // user 객체 생성
            modelAndView.addObject("isAdmin", user.isAdmin()); // 관리자 여부를 가져옴
            modelAndView.addObject("email", user.getEmail());
            modelAndView.addObject("nickname", user.getNickname());
        }else if (principal instanceof CustomOAuth2User){ // 소셜 이메일 가져오기
            String email = ((CustomOAuth2User) principal).getEmail();
            modelAndView.addObject("email", email);
        }
        Pair<ArticleVo[], PageVo_cy> articles;

        if (filter == null || filter.isEmpty() || keyword == null || keyword.isEmpty()) {
            articles = this.articleService.getArticlesByPaging(page);
        } else {
            articles = this.articleService.searchArticles(keyword, filter, page);
        }

        modelAndView.addObject("articles", articles.getLeft());
        modelAndView.addObject("pageVo", articles.getRight());
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("keyword", keyword);
        modelAndView.setViewName("board/list");
        return modelAndView;
    }
}