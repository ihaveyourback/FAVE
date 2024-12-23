package com.yhkim.fave.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"index"})
public class ArticleEntity {
    private int index;
    private String title;
    private String content;
    private String userEmail;
    private String userNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private LocalDateTime deletedAt;
    private int view;
}
