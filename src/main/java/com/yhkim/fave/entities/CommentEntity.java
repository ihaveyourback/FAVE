package com.yhkim.fave.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = {"index"})
public class CommentEntity {
    private int index;
    private int postId;
    private Integer commentId;
    private String comment;
    private String userEmail;
    private String userNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private LocalDateTime isDeleted;
}
