package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.CustomOAuth2User;
import com.yhkim.fave.entities.UserEntity;
import com.yhkim.fave.services.InquiriesArticleService;
import com.yhkim.fave.vos.InquiriesArticleVo;
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
@RequestMapping(value = "/inquiries")
public class InquiriesBoardController {

    private final InquiriesArticleService inquiriesArticleService;

    @Autowired
    public InquiriesBoardController(InquiriesArticleService inquiriesArticleService) {
        this.inquiriesArticleService = inquiriesArticleService;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getList(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "keyword", required = false) String keyword,
            @AuthenticationPrincipal Object principal,
            @AuthenticationPrincipal UserDetails userDetails) {

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
        Pair<InquiriesArticleVo[], PageVo_cy> articles;

        if (filter == null || filter.isEmpty() || keyword == null || keyword.isEmpty()) {
            articles = this.inquiriesArticleService.getArticlesByPaging(page);
        } else {
            articles = this.inquiriesArticleService.searchArticles(keyword, filter, page);
        }

        modelAndView.addObject("articles", articles.getLeft());
        modelAndView.addObject("pageVo", articles.getRight());
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("keyword", keyword);
        modelAndView.setViewName("Inquiries/list");
        return modelAndView;
    }
}