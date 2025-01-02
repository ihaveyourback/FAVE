package com.yhkim.fave.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardPostEntity {
    private Long index;
    private String title;
    private String content;
    private String userEmail;
    private String userNickname;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime deletedAt;
    private int view;
    private int like;

    private UserEntity user;
}
