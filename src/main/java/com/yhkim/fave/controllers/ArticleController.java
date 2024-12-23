package com.yhkim.fave.controllers;

import com.yhkim.fave.entities.ArticleEntity;
import com.yhkim.fave.entities.ImageEntity;
import com.yhkim.fave.results.article.ArticleResult;
import com.yhkim.fave.results.article.DeleteArticleResult;
import com.yhkim.fave.services.ArticleService;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
@RequestMapping(value = "/article")
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // 게시글 삭제
    @RequestMapping(value = "/read", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteRead(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        ArticleEntity article = articleService.getArticleByIndex(index);

        JSONObject response = new JSONObject();

        if (article == null || article.getDeletedAt() != null) { // 이미 삭제된 게시글이거나, 존재하지 않는 게시글인 경우
            response.put("result", "failure");
            response.put("message", "삭제된 게시글입니다.");
        } else {
            DeleteArticleResult result = this.articleService.deleteArticle(index);
            response.put("result", result.name().toLowerCase());
        }

        return response.toString();
    }

    // 게시글 수정 페이지 요청 처리
    @RequestMapping(value = "/modify", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getModify(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        ModelAndView modelAndView = new ModelAndView(); // 타임리프를 위한 ModelAndView 객체 생성
        ArticleEntity article = this.articleService.getArticleByIndex(index); // index로 게시글 조회
        if (article != null) {
            // 게시글이 존재하면 관련 데이터 추가
            modelAndView.addObject("article", article); // 게시글 정보 추가
        }
        modelAndView.setViewName("article/modify"); // modify.html로 연결
        return modelAndView;
    }

    // 게시글 수정 요청 처리 (PATCH)
    @RequestMapping(value = "/modify", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchModify(ArticleEntity article) {
        boolean result = this.articleService.modifyArticle(article); // 수정 로직 호출
        JSONObject response = new JSONObject(); // JSON 응답 객체 생성
        response.put("result", result); // 수정 결과 추가
        return response.toString(); // JSON 형식으로 응답
    }

    @RequestMapping(value = "/image", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        ImageEntity image = this.articleService.getImage(index);
        if (image == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .contentLength(image.getData().length)
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getData());
    }

    @RequestMapping(value = "/image", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postImage(@RequestParam(value = "upload") MultipartFile file) throws IOException {
        ImageEntity image = new ImageEntity();
        image.setData(file.getBytes()); // 업로드된 파일의 데이터 설정
        image.setContentType(file.getContentType()); // 업로드된 파일의 MIME 타입 설정
        image.setName(file.getOriginalFilename()); // 업로드된 파일의 원본 이름 설정
        JSONObject response = new JSONObject(); // JSON 응답 객체 생성
        boolean result = this.articleService.uploadImage(image); // 이미지 업로드 처리
        if (result) {
            // 업로드 성공 시 URL 정보를 응답 JSON에 추가
            response.put("url", "/article/image?index=" + image.getIndex());
        }
        return response.toString(); // JSON 형식으로 응답
    }

    @RequestMapping(value = "/read", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRead(HttpServletResponse response,
                                @RequestParam(value = "index", required = false) int index) {
        ArticleEntity article = articleService.getArticleByIndex(index);
        ModelAndView modelAndView = new ModelAndView();
        if (article != null) {
            articleService.increaseArticleView(article);
        }
        modelAndView.setViewName("article/read");
        modelAndView.addObject("article", article);
        response.setHeader("Cache-Control", "no-cache");
        return modelAndView;
    }

    @RequestMapping(value = "/write", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("article/write");
        return modelAndView;
    }

    @RequestMapping(value = "/write", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postWrite(ArticleEntity articleEntity) {

//        System.out.println("Received Title: " + articleEntity.getTitle());
//        System.out.println("Received Content: " + articleEntity.getContent());
//        System.out.println("Received UserEmail: " + articleEntity.getUserEmail());
//        System.out.println("Received UserNickname: " + articleEntity.getUserNickname());

        JSONObject response = new JSONObject();
        ArticleResult articleResult = this.articleService.write(articleEntity);
        response.put("result", articleResult.toString().toLowerCase());
        if (articleResult == ArticleResult.SUCCESS) {
            response.put("index", articleEntity.getIndex());
        }
        return response.toString();
    }
}
