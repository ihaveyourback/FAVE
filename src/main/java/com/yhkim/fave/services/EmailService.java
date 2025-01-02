package com.yhkim.fave.services;

import com.yhkim.fave.dto.MailDto;
import com.yhkim.fave.entities.SentEmailEntity;

import com.yhkim.fave.repository.SentEmailRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final SentEmailRepository sentEmailRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, SentEmailRepository sentEmailRepository) {
        this.mailSender = mailSender;
        this.sentEmailRepository = sentEmailRepository;
    }

    @Transactional
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
    }

    public void sendSimpleMessage(MailDto mailDto, SentEmailEntity sent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("dltjrals1018@gmail.com");
        message.setTo(mailDto.getAddress());
        message.setSubject(mailDto.getTitle());
        message.setText(mailDto.getContent());  // content가 올바르게 설정되어 있는지 확인

        // 제대로 이메일 값을 전달해야 함
        sent.setUserEmail(mailDto.getAddress());  // 변경: MailDto에서 받은 주소를 저장
        sent.setResponseAt(LocalDateTime.now());
        sentEmailRepository.save(sent); // 데이터베이스에 저장

        mailSender.send(message);
    }
}


