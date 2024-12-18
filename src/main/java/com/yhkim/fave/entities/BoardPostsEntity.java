package com.yhkim.fave.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardPostsEntity {
    private int index;
    private String title;
    private String content;
    private String userEmail;
    private String userNickName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private int view;

    private UserEntity user;
}

