package com.yhkim.fave.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InquiriesEntity {
    private int index;
    private String title;
    private String content;
    private String userEmail;
    private String userNickName;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime isDeleted;
    private String isResolved;
    private int view;

}
