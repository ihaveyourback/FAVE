package com.yhkim.fave.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "fave", name = "board_comments")
@Getter
@Setter
public class BoardCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`index`")
    private int commentIndex;

    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "user_email", length = 50)
    private String userEmail;

    @Column(name = "user_nickname", length = 10)
    private String userNickname;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private LocalDateTime isDeleted;

}
