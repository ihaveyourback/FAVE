package com.yhkim.fave.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "fave", name = "board_posts")
@Getter
@Setter
public class BoardPostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`index`")
    private int index;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "user_email", length = 50)
    private String userEmail;

    @Column(name = "user_nickname", length = 10)
    private String userNickname;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "view")
    private int view;

    @Column(name = "like_count")
    private int like;


    @Transient
    private UserEntity user;
}

