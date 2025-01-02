package com.yhkim.fave.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"index"})
public class InquiriesArticleEntity {
    private int index;
    private String title;
    private String content;
    private String userEmail;
    private String userNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private LocalDateTime isDeleted;
    private int view;
    private String isResolved = InquiriesStatus.PROCESSING.getDescription();
}
enum InquiriesStatus {
    PROCESSING("답변 대기"),
    COMPLETED("답변 완료");

    private final String InquiriesStatus;

    InquiriesStatus(String description) {
        this.InquiriesStatus = description;
    }

    public String getDescription() {
        return InquiriesStatus;
    }
}