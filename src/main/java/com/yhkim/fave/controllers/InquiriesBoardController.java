package com.yhkim.fave.controllers;

import com.yhkim.fave.services.InquiriesArticleService;
import com.yhkim.fave.vos.InquiriesArticleVo;
import com.yhkim.fave.vos.PageVo_cy;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
            @RequestParam(value = "keyword", required = false) String keyword) {

        ModelAndView modelAndView = new ModelAndView();
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