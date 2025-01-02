package com.yhkim.fave.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "fave", name = "sent_email")
@Getter
@Setter

public class SentEmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`index`")
    private int index;

    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "user_email" ,length = 50)
    private String userEmail;

    @Column(name = "user_nickname",length = 50)
    private String UserNickname;

    @Column(name = "content")
    private String content;

    @Column(name = "response_at")
    private LocalDateTime responseAt;
}
