package com.yhkim.fave.controllers;

import com.yhkim.fave.dto.MailDto;
import com.yhkim.fave.entities.InquiriesArticleEntity;
import com.yhkim.fave.entities.SentEmailEntity;
import com.yhkim.fave.services.EmailService;
import com.yhkim.fave.services.InquiriesArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmailController {
    private final EmailService emailService;
    private final InquiriesArticleService inquiriesArticleService;

    @Autowired
    public EmailController(EmailService emailService, InquiriesArticleService inquiriesArticleService) {
        this.emailService = emailService;
        this.inquiriesArticleService = inquiriesArticleService;
    }

    @GetMapping("/mail/send")
    public ModelAndView emailSend(@RequestParam(value = "index", required = false, defaultValue = "0") int index) {
        InquiriesArticleEntity article = this.inquiriesArticleService.getArticleByIndex(index);
        System.out.println("유저이메일:" + article.getUserEmail());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("article", article);
        modelAndView.setViewName("email/inquiries");
        return modelAndView;
    }

    @PostMapping("/mail/send")
    public String sendEmail(@RequestParam("address") String address,
                            @RequestParam("title") String title,
                            @RequestParam("content") String content,
                            @RequestParam("nickname") String nickname,
                            @RequestParam("post_id")Integer postId,
                            RedirectAttributes redirectAttributes) {

        // 폼에서 받은 값 확인
        System.out.println("Address: " + address);
        System.out.println("Title: " + title);
        System.out.println("Content: " + content);
        System.out.println("Nickname: " + nickname);
        System.out.println("Post ID: " + postId);  // 추가된 post_id

        // MailDto에 폼 값 설정
        MailDto mailDto = new MailDto();
        mailDto.setAddress(address);
        mailDto.setTitle(title);
        mailDto.setContent(content);

        // 이메일 전송 및 DB 저장
        SentEmailEntity sentEmailEntity = new SentEmailEntity();
        sentEmailEntity.setUserNickname(nickname);
        sentEmailEntity.setContent(content);  // content 값 설정 추가
        sentEmailEntity.setPostId(postId);  // post_id 값 설정
        emailService.sendSimpleMessage(mailDto, sentEmailEntity);

        // 리다이렉트 시 메시지 전달
//        redirectAttributes.addFlashAttribute("message", "메일이 성공적으로 발송되었습니다.");

        return "redirect:/inquiries/list";
    }
}

